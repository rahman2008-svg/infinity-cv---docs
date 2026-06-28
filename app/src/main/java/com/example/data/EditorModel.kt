package com.example.data

enum class LayerType {
    TEXT, IMAGE, QR_CODE, BARCODE, SIGNATURE, SHAPE
}

enum class ShapeType {
    RECTANGLE, CIRCLE, LINE, SHIELD
}

data class EditorLayer(
    val id: String,
    val type: LayerType,
    val text: String = "",
    val fontName: String = "System", // System, Serif, Monospace, Elegant, Handwriting (Bilingual support)
    val color: Int = 0xFF1E293B.toInt(), // default Slate 800
    val fontSize: Float = 16f,
    val x: Float = 50f,
    val y: Float = 100f,
    val width: Float = 180f,
    val height: Float = 40f,
    val rotation: Float = 0f,
    val qrCodeData: String = "https://ais-pre-2ausgzc2xnw6ik7lrc36gm-1071456034616.asia-southeast1.run.app",
    val barcodeData: String = "123456789012",
    val shapeType: ShapeType = ShapeType.RECTANGLE,
    val borderSize: Float = 0f,
    val isLocked: Boolean = false
)
