package mew.jorgecasariego.whatsappofthrones.models

class User(val uid: String, val username: String, val profileImageUrl: String) {
    constructor(): this("", "", "")
}