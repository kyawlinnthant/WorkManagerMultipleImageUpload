package com.klt.workmanagertest.data

data class UploadResponse(
    val data: UploadData?,
    val message: String,
    val status: String
){
    fun toVo() : UploadVo{
        return UploadVo(
            error = if (status == "FAIL") message else null,
            urls = data?.messageData?.map { it.toVo() }
        )
    }
}

data class UploadData(
    val isBlock: Boolean,
    val messageData: List<MessageData>
)

data class MessageData(
    val original: String,
    val thumbnail: String
){
    fun toVo() : UploadUrl{
        return UploadUrl(
            resolution = original,
            thumbnail = thumbnail
        )
    }
}

data class UploadVo(
    val error : String?,
    val urls : List<UploadUrl>?
)
data class UploadUrl(
    val resolution : String,
    val thumbnail : String
)