const express = require('express');
const router = express.Router();
const {insertMAC} = require('../controllers/contacts.js')
router.route('/contacts').get(insertMAC);
module.exports = router