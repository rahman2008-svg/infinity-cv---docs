package com.example.ui

import android.app.Application
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

sealed class AppScreen {
    object Home : AppScreen()
    data class FormFill(val templateId: String) : AppScreen()
    data class AdvancedEditor(val documentId: Int? = null, val initialTemplateId: String? = null) : AppScreen()
    object SavedDocs : AppScreen()
    object PremiumStore : AppScreen()
    object AboutDeveloper : AppScreen()
}

class AppViewModel(
    application: Application,
    private val repository: DocumentRepository
) : AndroidViewModel(application) {

    // --- State: Localization ---
    private val _language = MutableStateFlow("bn") // default to Bangla "bn", options: "bn", "en"
    val language: StateFlow<String> = _language.asStateFlow()

    fun toggleLanguage() {
        _language.value = if (_language.value == "en") "bn" else "en"
    }

    // --- State: Navigation Backstack ---
    private val _navigationStack = MutableStateFlow<List<AppScreen>>(listOf(AppScreen.Home))
    val currentScreen: StateFlow<AppScreen> = MutableStateFlow<AppScreen>(AppScreen.Home).apply {
        viewModelScope.launch {
            _navigationStack.collect { stack ->
                value = stack.lastOrNull() ?: AppScreen.Home
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, AppScreen.Home)

    fun navigateTo(screen: AppScreen) {
        val current = _navigationStack.value.toMutableList()
        current.add(screen)
        _navigationStack.value = current
    }

    fun navigateBack(): Boolean {
        val current = _navigationStack.value.toMutableList()
        if (current.size > 1) {
            current.removeAt(current.size - 1)
            _navigationStack.value = current
            return true
        }
        return false
    }

    // --- State: Premium / Pro Feature Lock ---
    private val _isPremiumUnlocked = MutableStateFlow(false)
    val isPremiumUnlocked: StateFlow<Boolean> = _isPremiumUnlocked.asStateFlow()

    fun togglePremium() {
        _isPremiumUnlocked.value = !_isPremiumUnlocked.value
    }

    // --- State: Saved Documents ---
    val savedDocuments: StateFlow<List<SavedDocument>> = repository.allDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteDocuments: StateFlow<List<SavedDocument>> = repository.favoriteDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- State: Form Filling ---
    val formValues = mutableStateMapOf<String, String>()
    private val _selectedTemplate = MutableStateFlow<DocumentTemplate?>(null)
    val selectedTemplate: StateFlow<DocumentTemplate?> = _selectedTemplate.asStateFlow()

    fun getTemplateById(templateId: String): DocumentTemplate? {
        var template = Templates.list.find { it.id == templateId }
        if (template == null && templateId.startsWith("dynamic_")) {
            val title = templateId.substringAfter("dynamic_").replace("_", " ").replaceFirstChar { it.uppercase() }
            val fields = when {
                title.contains("cert", ignoreCase = true) || title.contains("সনদ", ignoreCase = true) -> listOf(
                    TemplateField("recipient", "Recipient Name", "প্রাপকের নাম", "Prince AR Abdur Rahman"),
                    TemplateField("title", "Certificate Title", "সনদপত্রের শিরোনাম", "Certificate of Achievement"),
                    TemplateField("course", "Course/Topic Name", "কোর্স বা বিষয়", "Advanced Android Development"),
                    TemplateField("date", "Date of Issue", "প্রদানের তারিখ", "28-06-2026"),
                    TemplateField("authority", "Authorized Signatory", "অনুমোদনকারী ব্যক্তি", "NexVora Lab's Ofc")
                )
                title.contains("letter", ignoreCase = true) || title.contains("চিঠি", ignoreCase = true) || title.contains("আবেদন", ignoreCase = true) -> listOf(
                    TemplateField("date", "Date", "তারিখ", "28 June, 2026"),
                    TemplateField("recipient", "Recipient Name / Title", "প্রাপকের নাম ও পদবী", "The Managing Director"),
                    TemplateField("subject", "Subject", "বিষয়", "Application for custom assistance"),
                    TemplateField("body", "Body Content", "আবেদনের বিবরণ", "I am writing this to requesting standard dynamic templates. Thank you for your support.", true),
                    TemplateField("sender", "Sender Name", "প্রেরকের নাম", "Prince AR Abdur Rahman")
                )
                title.contains("bill", ignoreCase = true) || title.contains("invoice", ignoreCase = true) || title.contains("রসিদ", ignoreCase = true) -> listOf(
                    TemplateField("company", "Company / Business Name", "কোম্পানির নাম", "NexVora Lab's Ofc"),
                    TemplateField("invoice_no", "Invoice/Receipt Number", "ইনভয়েস বা রিসিট নং", "INV-2026-999"),
                    TemplateField("client", "Client Name", "গ্রাহকের নাম", "Abdur Rahman Prince"),
                    TemplateField("date", "Date", "তারিখ", "28-06-2026"),
                    TemplateField("item", "Item Details", "আইটেম বিবরণ", "Custom Software Development Support"),
                    TemplateField("amount", "Total Price (৳)", "মোট টাকা", "25000")
                )
                title.contains("card", ignoreCase = true) || title.contains("কার্ড", ignoreCase = true) -> listOf(
                    TemplateField("company", "Company Name", "প্রতিষ্ঠানের নাম", "NexVora Lab's Ofc"),
                    TemplateField("name", "Full Name", "নাম", "Prince AR Abdur Rahman"),
                    TemplateField("title", "Designation", "পদবী", "Independent App Developer"),
                    TemplateField("phone", "Phone Number", "মোবাইল নম্বর", "01707424006"),
                    TemplateField("email", "Email Address", "ইমেইল", "prince.ar.abdur.rahman200805@gmail.com")
                )
                else -> listOf(
                    TemplateField("title", "Document Title", "ডকুমেন্ট শিরোনাম", title),
                    TemplateField("date", "Date", "তারিখ", "28-06-2026"),
                    TemplateField("sub", "Sub-header", "উপ-শিরোনাম", "Official Document Summary"),
                    TemplateField("details", "Detailed Content", "মূল বিবরণ", "This is a custom-tailored smart template generated instantly. You can edit every single detail inside the smart editor.", true),
                    TemplateField("sign", "Signature Line Name", "স্বাক্ষরের নাম", "Prince AR Abdur Rahman")
                )
            }
            template = DocumentTemplate(
                id = templateId,
                titleEn = "Smart Template: $title",
                titleBn = "স্মার্ট টেমপ্লেট: $title",
                category = "Personal",
                fields = fields
            )
        }
        return template
    }

    fun loadTemplate(templateId: String) {
        val template = getTemplateById(templateId)
        _selectedTemplate.value = template
        formValues.clear()
        template?.fields?.forEach { field ->
            formValues[field.key] = field.defaultValue
        }
    }

    fun updateFormField(key: String, value: String) {
        formValues[key] = value
    }

    // --- State: Canvas / Advanced Editor ---
    private val _canvasLayers = MutableStateFlow<List<EditorLayer>>(emptyList())
    val canvasLayers: StateFlow<List<EditorLayer>> = _canvasLayers.asStateFlow()

    private val _selectedLayerId = MutableStateFlow<String?>(null)
    val selectedLayerId: StateFlow<String?> = _selectedLayerId.asStateFlow()

    // Undo / Redo Stacks
    private val undoStack = mutableListOf<List<EditorLayer>>()
    private val redoStack = mutableListOf<List<EditorLayer>>()

    private fun saveToUndo() {
        undoStack.add(_canvasLayers.value.toList())
        redoStack.clear()
        if (undoStack.size > 20) undoStack.removeAt(0) // limit stack
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val previous = undoStack.removeAt(undoStack.size - 1)
            redoStack.add(_canvasLayers.value.toList())
            _canvasLayers.value = previous
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val next = redoStack.removeAt(redoStack.size - 1)
            undoStack.add(_canvasLayers.value.toList())
            _canvasLayers.value = next
        }
    }

    // Advanced Editor Operations
    fun initializeEditorWithTemplate(templateId: String) {
        val template = getTemplateById(templateId) ?: return
        saveToUndo()
        val list = mutableListOf<EditorLayer>()
        
        // Add a primary background frame layer
        list.add(
            EditorLayer(
                id = "bg_frame",
                type = LayerType.SHAPE,
                shapeType = ShapeType.RECTANGLE,
                color = 0xFFFFFFFF.toInt(),
                x = 10f,
                y = 10f,
                width = 340f,
                height = 500f,
                borderSize = 2f,
                isLocked = true
            )
        )

        // Add Document Title Header
        list.add(
            EditorLayer(
                id = "header_title",
                type = LayerType.TEXT,
                text = template.titleEn,
                fontSize = 20f,
                color = 0xFF0F172A.toInt(), // Dark slate
                x = 20f,
                y = 25f,
                width = 320f,
                fontName = "Serif"
            )
        )

        // Populate fields as text layers
        var currentY = 80f
        template.fields.forEach { field ->
            val value = formValues[field.key] ?: field.defaultValue
            list.add(
                EditorLayer(
                    id = "label_${field.key}",
                    type = LayerType.TEXT,
                    text = "${field.labelEn} / ${field.labelBn}:",
                    fontSize = 11f,
                    color = 0xFF64748B.toInt(), // Gray
                    x = 25f,
                    y = currentY,
                    width = 310f
                )
            )
            currentY += 18f
            list.add(
                EditorLayer(
                    id = "value_${field.key}",
                    type = LayerType.TEXT,
                    text = value,
                    fontSize = 13f,
                    color = 0xFF1E293B.toInt(), // Dark Slate
                    x = 25f,
                    y = currentY,
                    width = 310f,
                    fontName = "SansSerif"
                )
            )
            currentY += 30f
        }

        // Add Watermark if official
        if (template.isOfficialWatermarked) {
            list.add(
                EditorLayer(
                    id = "watermark",
                    type = LayerType.TEXT,
                    text = "SAMPLE / DEMO / NOT FOR OFFICIAL USE",
                    fontSize = 12f,
                    color = 0x2EEF4444.toInt(), // Very light alpha red
                    x = 20f,
                    y = 220f,
                    width = 320f,
                    rotation = -25f,
                    fontName = "Monospace"
                )
            )
        }

        _canvasLayers.value = list
        _selectedLayerId.value = null
    }

    fun initializeEditorWithSavedDoc(document: SavedDocument) {
        saveToUndo()
        val layers = parseLayersJson(document.detailsJson)
        _canvasLayers.value = layers
        _selectedLayerId.value = null
    }

    fun addTextLayer(text: String) {
        saveToUndo()
        val newLayer = EditorLayer(
            id = "text_${UUID.randomUUID()}",
            type = LayerType.TEXT,
            text = text,
            x = 80f,
            y = 150f
        )
        _canvasLayers.value = _canvasLayers.value + newLayer
        _selectedLayerId.value = newLayer.id
    }

    fun addShapeLayer(shapeType: ShapeType, color: Int) {
        saveToUndo()
        val newLayer = EditorLayer(
            id = "shape_${UUID.randomUUID()}",
            type = LayerType.SHAPE,
            shapeType = shapeType,
            color = color,
            x = 80f,
            y = 150f,
            width = 100f,
            height = 100f
        )
        _canvasLayers.value = _canvasLayers.value + newLayer
        _selectedLayerId.value = newLayer.id
    }

    fun addQrLayer(qrText: String) {
        saveToUndo()
        val newLayer = EditorLayer(
            id = "qr_${UUID.randomUUID()}",
            type = LayerType.QR_CODE,
            qrCodeData = qrText,
            x = 100f,
            y = 350f,
            width = 100f,
            height = 100f
        )
        _canvasLayers.value = _canvasLayers.value + newLayer
        _selectedLayerId.value = newLayer.id
    }

    fun addBarcodeLayer(barcodeText: String) {
        saveToUndo()
        val newLayer = EditorLayer(
            id = "barcode_${UUID.randomUUID()}",
            type = LayerType.BARCODE,
            barcodeData = barcodeText,
            x = 80f,
            y = 400f,
            width = 180f,
            height = 45f
        )
        _canvasLayers.value = _canvasLayers.value + newLayer
        _selectedLayerId.value = newLayer.id
    }

    fun addSignatureLayer() {
        saveToUndo()
        val newLayer = EditorLayer(
            id = "sig_${UUID.randomUUID()}",
            type = LayerType.SIGNATURE,
            color = 0xFF0000FF.toInt(), // Ink blue signature
            x = 120f,
            y = 420f,
            width = 120f,
            height = 50f
        )
        _canvasLayers.value = _canvasLayers.value + newLayer
        _selectedLayerId.value = newLayer.id
    }

    fun selectLayer(id: String?) {
        _selectedLayerId.value = id
    }

    fun updateSelectedLayer(updateBlock: (EditorLayer) -> EditorLayer) {
        val layerId = _selectedLayerId.value ?: return
        saveToUndo()
        _canvasLayers.value = _canvasLayers.value.map { layer ->
            if (layer.id == layerId) updateBlock(layer) else layer
        }
    }

    fun deleteSelectedLayer() {
        val layerId = _selectedLayerId.value ?: return
        saveToUndo()
        _canvasLayers.value = _canvasLayers.value.filter { it.id != layerId }
        _selectedLayerId.value = null
    }

    fun duplicateSelectedLayer() {
        val layerId = _selectedLayerId.value ?: return
        val layerToDuplicate = _canvasLayers.value.find { it.id == layerId } ?: return
        saveToUndo()
        val newLayer = layerToDuplicate.copy(
            id = "${layerToDuplicate.type.name.lowercase()}_${UUID.randomUUID()}",
            x = (layerToDuplicate.x + 15f).coerceIn(-50f, 350f),
            y = (layerToDuplicate.y + 15f).coerceIn(-50f, 550f),
            isLocked = false
        )
        _canvasLayers.value = _canvasLayers.value + newLayer
        _selectedLayerId.value = newLayer.id
    }

    fun moveSelectedLayerUp() {
        val layerId = _selectedLayerId.value ?: return
        val currentList = _canvasLayers.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == layerId }
        if (index != -1 && index < currentList.size - 1) {
            saveToUndo()
            val item = currentList.removeAt(index)
            currentList.add(index + 1, item)
            _canvasLayers.value = currentList
        }
    }

    fun moveSelectedLayerDown() {
        val layerId = _selectedLayerId.value ?: return
        val currentList = _canvasLayers.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == layerId }
        if (index > 0) {
            saveToUndo()
            val item = currentList.removeAt(index)
            currentList.add(index - 1, item)
            _canvasLayers.value = currentList
        }
    }

    fun toggleSelectedLayerLock() {
        val layerId = _selectedLayerId.value ?: return
        saveToUndo()
        _canvasLayers.value = _canvasLayers.value.map { layer ->
            if (layer.id == layerId) layer.copy(isLocked = !layer.isLocked) else layer
        }
    }

    fun addImageLayer(presetName: String) {
        saveToUndo()
        val newLayer = EditorLayer(
            id = "image_${UUID.randomUUID()}",
            type = LayerType.IMAGE,
            text = presetName, // using text as image/preset identifier
            x = 80f,
            y = 180f,
            width = 100f,
            height = 100f
        )
        _canvasLayers.value = _canvasLayers.value + newLayer
        _selectedLayerId.value = newLayer.id
    }

    fun dragLayer(id: String, deltaX: Float, deltaY: Float) {
        _canvasLayers.value = _canvasLayers.value.map { layer ->
            if (layer.id == id && !layer.isLocked) {
                layer.copy(
                    x = (layer.x + deltaX).coerceIn(-100f, 400f),
                    y = (layer.y + deltaY).coerceIn(-100f, 600f)
                )
            } else layer
        }
    }

    fun resizeLayer(id: String, deltaWidth: Float, deltaHeight: Float) {
        _canvasLayers.value = _canvasLayers.value.map { layer ->
            if (layer.id == id && !layer.isLocked) {
                layer.copy(
                    width = (layer.width + deltaWidth).coerceAtLeast(30f),
                    height = (layer.height + deltaHeight).coerceAtLeast(10f)
                )
            } else layer
        }
    }

    // --- State: Database Operations ---
    fun saveFormAsDocument(customTitle: String) {
        val template = _selectedTemplate.value ?: return
        viewModelScope.launch {
            val detailsJson = mapToJson(formValues)
            val document = SavedDocument(
                title = customTitle.ifBlank { template.titleEn },
                category = template.category,
                subCategory = template.titleEn,
                detailsJson = detailsJson
            )
            repository.insertDocument(document)
        }
    }

    fun saveAdvancedEditorDocument(customTitle: String, editingDocId: Int? = null) {
        viewModelScope.launch {
            val jsonContent = serializeLayers(_canvasLayers.value)
            val document = SavedDocument(
                id = editingDocId ?: 0,
                title = customTitle.ifBlank { "Custom Document" },
                category = "Editor",
                subCategory = "Advanced Design",
                detailsJson = jsonContent
            )
            repository.insertDocument(document)
        }
    }

    fun toggleFavorite(document: SavedDocument) {
        viewModelScope.launch {
            repository.updateDocument(document.copy(isFavorite = !document.isFavorite))
        }
    }

    fun deleteDocument(document: SavedDocument) {
        viewModelScope.launch {
            repository.deleteDocument(document)
        }
    }

    fun setDocumentPassword(document: SavedDocument, password: String?) {
        viewModelScope.launch {
            repository.updateDocument(document.copy(passwordLock = password))
        }
    }

    // --- Custom Micro-Serialization Systems ---
    private fun mapToJson(map: Map<String, String>): String {
        return map.entries.joinToString(separator = ",", prefix = "{", postfix = "}") {
            "\"${it.key.replace("\"", "\\\"")}\":\"${it.value.replace("\"", "\\\"")}\""
        }
    }

    fun jsonToMap(json: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val trimmed = json.trim().removePrefix("{").removeSuffix("}")
        if (trimmed.isEmpty()) return map
        
        // Custom simple parser that handles commas inside quotes correctly
        var inQuotes = false
        val currentPair = StringBuilder()
        val pairs = mutableListOf<String>()
        
        for (char in trimmed) {
            if (char == '"') {
                inQuotes = !inQuotes
            }
            if (char == ',' && !inQuotes) {
                pairs.add(currentPair.toString())
                currentPair.clear()
            } else {
                currentPair.append(char)
            }
        }
        if (currentPair.isNotEmpty()) {
            pairs.add(currentPair.toString())
        }

        for (pair in pairs) {
            val splitIdx = pair.indexOf(':')
            if (splitIdx != -1) {
                val key = pair.substring(0, splitIdx).trim().removePrefix("\"").removeSuffix("\"")
                val value = pair.substring(splitIdx + 1).trim().removePrefix("\"").removeSuffix("\"")
                map[key] = value.replace("\\\"", "\"")
            }
        }
        return map
    }

    private fun serializeLayers(layers: List<EditorLayer>): String {
        return layers.joinToString(separator = "|", prefix = "[", postfix = "]") { layer ->
            listOf(
                "id:${layer.id}",
                "type:${layer.type.name}",
                "text:${layer.text.replace("|", "~")}",
                "fontName:${layer.fontName}",
                "color:${layer.color}",
                "fontSize:${layer.fontSize}",
                "x:${layer.x}",
                "y:${layer.y}",
                "width:${layer.width}",
                "height:${layer.height}",
                "rotation:${layer.rotation}",
                "qrCodeData:${layer.qrCodeData}",
                "barcodeData:${layer.barcodeData}",
                "shapeType:${layer.shapeType.name}",
                "borderSize:${layer.borderSize}",
                "isLocked:${if (layer.isLocked) 1 else 0}"
            ).joinToString(",")
        }
    }

    private fun parseLayersJson(json: String): List<EditorLayer> {
        val list = mutableListOf<EditorLayer>()
        val trimmed = json.trim()
        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) return list
        val body = trimmed.removePrefix("[").removeSuffix("]")
        if (body.isEmpty()) return list
        
        val itemStrings = body.split("|")
        for (itemStr in itemStrings) {
            val parts = itemStr.split(",")
            var id = ""
            var type = LayerType.TEXT
            var text = ""
            var fontName = "System"
            var color = 0xFF000000.toInt()
            var fontSize = 14f
            var x = 50f
            var y = 100f
            var width = 150f
            var height = 40f
            var rotation = 0f
            var qrCodeData = ""
            var barcodeData = ""
            var shapeType = ShapeType.RECTANGLE
            var borderSize = 0f
            var isLocked = false

            for (part in parts) {
                val colIdx = part.indexOf(':')
                if (colIdx != -1) {
                    val key = part.substring(0, colIdx).trim()
                    val value = part.substring(colIdx + 1).trim()
                    when (key) {
                        "id" -> id = value
                        "type" -> type = LayerType.valueOf(value)
                        "text" -> text = value.replace("~", "|")
                        "fontName" -> fontName = value
                        "color" -> color = value.toIntOrNull() ?: 0xFF000000.toInt()
                        "fontSize" -> fontSize = value.toFloatOrNull() ?: 14f
                        "x" -> x = value.toFloatOrNull() ?: 50f
                        "y" -> y = value.toFloatOrNull() ?: 100f
                        "width" -> width = value.toFloatOrNull() ?: 150f
                        "height" -> height = value.toFloatOrNull() ?: 40f
                        "rotation" -> rotation = value.toFloatOrNull() ?: 0f
                        "qrCodeData" -> qrCodeData = value
                        "barcodeData" -> barcodeData = value
                        "shapeType" -> shapeType = ShapeType.valueOf(value)
                        "borderSize" -> borderSize = value.toFloatOrNull() ?: 0f
                        "isLocked" -> isLocked = value == "1"
                    }
                }
            }
            if (id.isNotEmpty()) {
                list.add(
                    EditorLayer(
                        id = id,
                        type = type,
                        text = text,
                        fontName = fontName,
                        color = color,
                        fontSize = fontSize,
                        x = x,
                        y = y,
                        width = width,
                        height = height,
                        rotation = rotation,
                        qrCodeData = qrCodeData,
                        barcodeData = barcodeData,
                        shapeType = shapeType,
                        borderSize = borderSize,
                        isLocked = isLocked
                    )
                )
            }
        }
        return list
    }
}

class AppViewModelFactory(
    private val application: Application,
    private val repository: DocumentRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
