const express = require('express');
const router = express.Router();
const { insertMAC, reportContacts } = require('../controllers/contacts.js')
router.route('/report').post(reportContacts);
module.exports = router