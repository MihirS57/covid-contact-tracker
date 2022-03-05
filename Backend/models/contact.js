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
                unique: true
            },
            contacts: [
                {
                    contact_mac: {
                        type: String,
                        unique: true
                    },
                    rmmi: Number
                }
            ]            
        }
    ]
})

module.exports = mongoose.model('Contact',contactSchema);