const express = require('express');
const router = express.Router();
const { createUser } = require('../controllers/users.js')
router.route('/create').post(createUser);
module.exports = router