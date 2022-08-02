const mongoose = require('mongoose')
const contactSchema = mongoose.Schema({
    joint_mac: {
        type: String,
        required: [true, 'Create Joint Mac'],
        unique: true
    },
    contact_mac_one: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'User',
        required: [true,'MAC address is mandatory']
    },
    contact_mac_two: {
        type: mongoose.Schema.ObjectId,
        ref: 'User',
        required: [true,'MAC Address is mandatory']
    },
    date: {
        type: String,
        required: [true, 'Date is mandatory'],
    },
    proximity: {
        type: Number,
        required: [true, 'Proximity is Mandatory']
    }
})

module.exports = mongoose.model('Contact',contactSchema);