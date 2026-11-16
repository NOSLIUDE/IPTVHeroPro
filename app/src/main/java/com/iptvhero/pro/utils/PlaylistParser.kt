package seu.pacote.utils // Substitua pelo seu pacote real

data class ParsedPlaylist(
    val profileName: String,
    val items: List<PlaylistItem>
)

data class PlaylistItem(
    val title: String,
    val url: String
)

object PlaylistParser {

    fun parseBatchTxtFile(fileContent: String): List<ParsedPlaylist> {
        val result = mutableListOf<ParsedPlaylist>()
        val lines = fileContent.lines()
        
        var currentProfileName = "Lista Importada 1"
        var currentItems = mutableListOf<PlaylistItem>()
        var listCounter = 1

        for (rawLine in lines) {
            val line = rawLine.trim()
            if (line.isEmpty()) continue

            // Detecta cabeçalhos (ex: [Nome da Lista] ou marcadores)
            if (isPlaylistHeader(line)) {
                if (currentItems.isNotEmpty()) {
                    result.add(ParsedPlaylist(currentProfileName, currentItems.toList()))
                    currentItems.clear()
                }
                currentProfileName = cleanHeaderName(line) ?: "Lista ${++listCounter}"
                continue
            }

            // Processa a linha do canal
            val parsedItem = parseLineToPlaylistItem(line)
            if (parsedItem != null) {
                currentItems.add(parsedItem)
            }
        }

        if (currentItems.isNotEmpty()) {
            result.add(ParsedPlaylist(currentProfileName, currentItems.toList()))
        }

        return result
    }

    private fun isPlaylistHeader(line: String): Boolean {
        return (line.startsWith("[") && line.endsWith("]")) || 
               (line.startsWith("#") && !line.contains("http") && line.length > 3) ||
               line.startsWith("---") || 
               line.startsWith("===")
    }

    private fun cleanHeaderName(line: String): String? {
        return line.removeSurrounding("[", "]")
                   .removePrefix("#")
                   .removePrefix("---")
                   .removeSuffix("---")
                   .trim()
                   .takeIf { it.isNotEmpty() }
    }

    private fun parseLineToPlaylistItem(line: String): PlaylistItem? {
        val parts = line.split(",", limit = 2)
        return if (parts.size == 2) {
            PlaylistItem(title = parts[0].trim(), url = parts[1].trim())
        } else if (line.startsWith("http://") || line.startsWith("https://")) {
            PlaylistItem(title = "Canal Sem Nome", url = line)
        } else {
            null
        }
    }
}
