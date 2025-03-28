package ci.miage.mob.networkdk

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat

class GraphView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var selectedNode: Graph.Node? = null
    private var selectedConnection: Graph.Connection? = null
    private var touchOffset = PointF()

    private val graph = Graph()
    private val nodeRadius = 50f
    private val defaultNodeColor = Color.BLUE
    private val defaultConnectionColor = Color.BLACK
    private val defaultConnectionStrokeWidth = 10f

    private var touchDownTime: Long = 0
    private var touchDownPosition: PointF? = null

    private var isConnectionMode = false
    private var tempConnectionStart: Graph.Node? = null
    private var tempConnectionEnd: PointF? = null
    private var tempConnectionLabel: String = ""

    private val nodePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 24f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private val textBackgroundPaint = Paint().apply {
        color = Color.argb(128, 0, 0, 0)
    }

    private val connectionPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = defaultConnectionStrokeWidth
        color = defaultConnectionColor
    }

    private val connectionLabelPaint = Paint().apply {
        color = Color.BLACK
        textSize = 20f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private val connectionLabelBackgroundPaint = Paint().apply {
        color = Color.argb(200, 255, 255, 255)
    }


    interface GraphViewListener {
        fun onNodeLongClick(node: Graph.Node)
        fun onConnectionLongClick(connection: Graph.Connection)
        fun showConnectionLabelDialog(startNode: Graph.Node, endNode: Graph.Node)
    }

    var listener: GraphViewListener? = null

    fun initializeGraph() {
        for (i in 0 until 2) {
            for (j in 0 until 3) {
                graph.addNode(PointF(100f + j * 200f, 100f + i * 200f), "defaut")
            }
        }
        invalidate()
    }

    fun completeConnection(label: String) {
        if (tempConnectionStart != null && tempConnectionEnd != null) {
        }
        tempConnectionStart = null
        tempConnectionEnd = null
        tempConnectionLabel = ""
        invalidate()
    }

    fun addNode() {
        val nodes = graph.getNodes()
        val lastNode = nodes.lastOrNull()
        val newX = lastNode?.position?.x ?: 100f
        val newY = lastNode?.position?.y ?: 100f

        if (nodes.size % 3 == 0) {
            graph.addNode(PointF(100f, newY + 200f), "defaut")
        } else {
            graph.addNode(PointF(newX + 200f, newY), "defaut")
        }
        invalidate()
    }

    fun addConnection(start: Graph.Node, end: Graph.Node, label: String = ""): Boolean {
        return graph.addConnection(start, end, label)
    }

    fun addNodeWithLabel(positions: PointF, label: String, color: Int = defaultNodeColor, imageRes: Int? = null) {
        val nodes = graph.getNodes()
        val lastNode = nodes.lastOrNull()
        val newX = lastNode?.position?.x ?: 100f
        val newY = lastNode?.position?.y ?: 100f
        if(nodes.size %3 ==0) {
            positions.x = 100f
            positions.y=newY+200f
            graph.addNode(positions, label, color, imageRes)
            invalidate()
        }else{
            positions.x = newX+200f
            positions.y=newY
            graph.addNode(positions, label, color, imageRes)
            invalidate()
        }
    }


    fun startConnectionMode() {
        isConnectionMode = true;
        tempConnectionStart = null;
        tempConnectionEnd = null;
    }

    fun resetConnections() {
        graph.clearConnections()
        invalidate()
    }

    fun cancelConnection() {
        tempConnectionStart = null;
        tempConnectionEnd = null;
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Dessin des connexions
        for (connection in graph.getConnections()) {
            connectionPaint.color = connection.color
            connectionPaint.strokeWidth = connection.strokeWidth

            val start = connection.start.position
            val end = connection.end.position

            // Dessiner la ligne de connexion
            canvas.drawLine(start.x, start.y, end.x, end.y, connectionPaint)

            // Dessiner l'étiquette de la connexion
            val midX = (start.x + end.x) / 2
            val midY = (start.y + end.y) / 2

            // Offset pour que l'étiquette ne soit pas exactement sur la ligne
            val angle = Math.atan2((end.y - start.y).toDouble(), (end.x - start.x).toDouble())
            val offsetX = 20 * Math.sin(angle).toFloat()
            val offsetY = -20 * Math.cos(angle).toFloat()

            // Fond de l'étiquette
            val textWidth = connectionLabelPaint.measureText(connection.label)
            canvas.drawRect(
                midX + offsetX - textWidth / 2 - 5,
                midY + offsetY - connectionLabelPaint.textSize - 5,
                midX + offsetX + textWidth / 2 + 5,
                midY + offsetY + 5,
                connectionLabelBackgroundPaint
            )

            // Texte de l'étiquette
            canvas.drawText(
                connection.label,
                midX + offsetX,
                midY + offsetY,
                connectionLabelPaint
            )
        }

        // Dessin des noeuds
        for (node in graph.getNodes()) {
            // Dessiner l'image ou le cercle
            node.imageRes?.let { resId ->
                ResourcesCompat.getDrawable(resources, resId, null)?.let { drawable ->
                    drawable.setBounds(
                        (node.position.x - nodeRadius).toInt(),
                        (node.position.y - nodeRadius).toInt(),
                        (node.position.x + nodeRadius).toInt(),
                        (node.position.y + nodeRadius).toInt()
                    )
                    drawable.draw(canvas)
                }
            } ?: run {
                nodePaint.color = node.color
                canvas.drawCircle(node.position.x, node.position.y, nodeRadius, nodePaint)
            }

            // Fond pour le texte
            canvas.drawRect(
                node.position.x - textPaint.measureText(node.label) / 2 - 10,
                node.position.y + nodeRadius,
                node.position.x + textPaint.measureText(node.label) / 2 + 10,
                node.position.y + nodeRadius + textPaint.textSize + 10,
                textBackgroundPaint
            )

            // Texte
            canvas.drawText(
                node.label,
                node.position.x,
                node.position.y + nodeRadius + textPaint.textSize / 2,
                textPaint
            )
        }

        // Dessin de la connexion temporaire
        tempConnectionStart?.let { start ->
            tempConnectionEnd?.let { end ->
                connectionPaint.color = defaultConnectionColor
                connectionPaint.strokeWidth = defaultConnectionStrokeWidth
                canvas.drawLine(
                    start.position.x,
                    start.position.y,
                    end.x,
                    end.y,
                    connectionPaint
                )
            }
        }
    }

    fun activeEdit() {
        isConnectionMode = false
    }

    fun removeSelectedNode() {
        selectedNode?.let {
            graph.removeNode(it)
            selectedNode = null
            invalidate()
        }
    }

    fun removeSelectedConnection() {
        selectedConnection?.let {
            graph.removeConnection(it)
            selectedConnection = null
            invalidate()
        }
    }

    fun updateNode(node: Graph.Node, label: String, color: Int, imageRes: Int?) {
        node.label = label
        node.color = color
        node.imageRes = imageRes
        invalidate()
    }

    fun updateConnection(connection: Graph.Connection, label: String, color: Int, strokeWidth: Float) {
        connection.label = label
        connection.color = color
        connection.strokeWidth = strokeWidth
        invalidate()
    }

    private fun findNodeAt(x: Float, y: Float): Graph.Node? {
        for (node in graph.getNodes()) {
            if (Math.hypot(
                    (node.position.x - x).toDouble(),
                    (node.position.y - y).toDouble()
                ) <= nodeRadius
            ) {
                return node
            }
        }
        return null
    }

    private fun findConnectionAt(x: Float, y: Float): Graph.Connection? {
        for (connection in graph.getConnections()) {
            val start = connection.start.position
            val end = connection.end.position

            // Vérifier si le point est proche de la ligne
            if (isPointNearLine(x, y, start.x, start.y, end.x, end.y)) {
                return connection
            }

            // Vérifier si le point est proche de l'étiquette
            val midX = (start.x + end.x) / 2
            val midY = (start.y + end.y) / 2
            val angle = Math.atan2((end.y - start.y).toDouble(), (end.x - start.x).toDouble())
            val offsetX = 20 * Math.sin(angle).toFloat()
            val offsetY = -20 * Math.cos(angle).toFloat()

            val labelX = midX + offsetX
            val labelY = midY + offsetY
            val textWidth = connectionLabelPaint.measureText(connection.label)

            if (x >= labelX - textWidth / 2 - 5 && x <= labelX + textWidth / 2 + 5 &&
                y >= labelY - connectionLabelPaint.textSize - 5 && y <= labelY + 5) {
                return connection
            }
        }
        return null
    }

    private fun isPointNearLine(px: Float, py: Float, x1: Float, y1: Float, x2: Float, y2: Float, tolerance: Float = 15f): Boolean {
        // Calcul de la distance entre le point et la ligne
        val A = px - x1
        val B = py - y1
        val C = x2 - x1
        val D = y2 - y1

        val dot = A * C + B * D
        val len_sq = C * C + D * D
        var param = -1f
        if (len_sq != 0f) param = dot / len_sq

        var xx: Float
        var yy: Float

        if (param < 0) {
            xx = x1
            yy = y1
        } else if (param > 1) {
            xx = x2
            yy = y2
        } else {
            xx = x1 + param * C
            yy = y1 + param * D
        }

        val dx = px - xx
        val dy = py - yy
        return Math.sqrt((dx * dx + dy * dy).toDouble()) <= tolerance
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {

            MotionEvent.ACTION_DOWN -> {
                touchDownTime = System.currentTimeMillis()
                touchDownPosition = PointF(event.x, event.y)

                if (isConnectionMode) {
                    tempConnectionStart = findNodeAt(event.x, event.y)
                    if (tempConnectionStart != null) {
                        tempConnectionEnd = tempConnectionStart!!.position
                        tempConnectionLabel = ""
                    }
                } else {
                    selectedNode = findNodeAt(event.x, event.y)
                    //selectedConnection = findConnectionAt(event.x, event.y)
                    if (selectedNode == null) {
                        selectedConnection = findConnectionAt(event.x, event.y)
                    }
                    selectedNode?.let {
                        touchOffset.set(
                            event.x - it.position.x,
                            event.y - it.position.y
                        )
                    }
                }
                invalidate()
            }

            MotionEvent.ACTION_MOVE -> {
                if (isConnectionMode && tempConnectionStart != null) {
                    tempConnectionEnd = PointF(event.x, event.y)
                    invalidate()
                } else {
                    selectedNode?.let {
                        it.position.set(
                            event.x - touchOffset.x,
                            event.y - touchOffset.y
                        )
                        invalidate()
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                val duration = System.currentTimeMillis() - touchDownTime

                if (isConnectionMode) {
                    if (tempConnectionStart != null) {
                        val endNode = findNodeAt(event.x, event.y)
                        if (endNode != null && endNode != tempConnectionStart) {
                            // Demander l'étiquette de la connexion
                            listener?.showConnectionLabelDialog(tempConnectionStart!!, endNode)
                        } else {
                            cancelConnection()
                        }
                    }
                } else if (duration >= 1000) { // Long click
                    touchDownPosition?.let { position ->
                        // Vérifier d'abord les nœuds
                        selectedNode = findNodeAt(position.x, position.y)
                        selectedNode?.let { node ->
                            listener?.onNodeLongClick(node)
                            return true
                        }

                        // Si aucun nœud, vérifier les étiquettes de connexion
                        selectedConnection = findConnectionAt(position.x, position.y)
                        selectedConnection?.let { connection ->
                            listener?.onConnectionLongClick(connection)
                        }
                    }
                }

                /*selectedNode = null
                touchDownPosition = null*/
                invalidate()
            }
        }
        return true
    }


}