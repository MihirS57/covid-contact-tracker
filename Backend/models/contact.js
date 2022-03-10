const mongoose = require('mongoose')
const contactSchema = mongoose.Schema({
    user_mac: {
        type: String,
        required: [true,'MAC Address is mandatory'],
        unique: true
    },
    contacts: [
        {
            date: {
                type: String,
                required: [true,'Date is mandatory'],
            },
            contacts: [
                {
                    contact_mac: {
                        type: String,
                    },
                    rmmi: Number
                }
            ]            
        }
    ]
})

module.exports = mongoose.model('Contact',contactSchema);