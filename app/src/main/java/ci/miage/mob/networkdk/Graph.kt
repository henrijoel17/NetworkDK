package ci.miage.mob.networkdk

import android.graphics.Color
import android.graphics.PointF
import android.util.Log

class Graph {

    private val nodes = mutableListOf<Node>()
    private val connections = mutableSetOf<Connection>()

    data class Node(var position: PointF, var label: String, var color: Int = Color.BLUE, var imageRes: Int? = null)

    data class Connection(
        val start: Node,
        val end: Node,
        var label: String = "",
        var color: Int = Color.BLACK,
        var strokeWidth: Float = 10f
    ){
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Connection) return false
            return (start == other.start && end == other.end) ||
                    (start == other.end && end == other.start)
        }

        override fun hashCode(): Int {
            return start.hashCode() + end.hashCode() // Doit être symétrique
        }
    }


    fun addNode(position: PointF, label: String, color: Int = Color.BLUE, imageRes: Int? = null) {
        nodes.add(Node(position, label, color, imageRes))
    }

    fun addConnection(start: Node, end: Node, label: String = ""): Boolean {
        if (start == end) return false // Empêche les boucles (connexion à soi-même)

        val newConnection = Connection(start, end, label)
        if (!connections.contains(newConnection)) {
            connections.add(newConnection)
            return true
        }
        return false // Connexion déjà existante
    }

    fun removeNode(node: Node) {
        nodes.remove(node)
        connections.removeIf { it.start == node || it.end == node }
    }

    fun removeConnection(connection: Connection) {
        connections.remove(connection)
    }

    fun clearConnections() {
        connections.clear()
    }

    fun getNodes(): List<Node> {
        return nodes
    }

    fun getConnections(): Set<Connection> {
        return connections
    }
}