const mongoose = require('mongoose')
const contactSchema = mongoose.Schema({
    user_mac: {
        type: String,
        required: ['true','MAC Address is mandatory'],
        unique: true
    },
    contacts: [
        {
            contact_mac: String,
            date: Date,
            rmmi: Number
        }
    ]
})

module.exports = mongoose.model('Contact',contactSchema);