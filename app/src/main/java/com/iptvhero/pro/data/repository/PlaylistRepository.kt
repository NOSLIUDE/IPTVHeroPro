// Dentro do seu Repository ou ViewModel existente:
suspend fun importBatchTxt(fileContent: String, profileDao: ProfileDao, channelDao: ChannelDao) {
    // 1. Usa o parser criado no Passo 1 para quebrar o TXT em várias listas
    val parsedLists = PlaylistParser.parseBatchTxtFile(fileContent)

    for (playlist in parsedLists) {
        // 2. Cria e insere o perfil automaticamente no Room, obtendo o ID gerado
        val newProfile = ProfileEntity(name = playlist.profileName)
        val profileId = profileDao.insertProfile(newProfile) 

        // 3. Mapeia e insere todos os canais vinculados àquele ID de perfil
        val channelEntities = playlist.items.map { item ->
            ChannelEntity(
                profileId = profileId,
                title = item.title,
                url = item.url
            )
        }
        channelDao.insertChannels(channelEntities)
    }
}
