const mongoose = require('mongoose')
const contactSchema = mongoose.Schema({
    user_mac: {
        type: String,
        required: [true, 'MAC Address is mandatory'],
        unique: true
    },
    covid_flag: {
        type: Boolean,
        required: [true, 'Covid Status is mandatory']
    },
    contacts: [
        {
            type: mongoose.Schema.ObjectId,
            ref: 'Contact',
            required: [false]
        }
    ]
})

module.exports = mongoose.model('User', contactSchema);