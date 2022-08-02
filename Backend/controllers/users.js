const User = require('../models/users')
const Contact = require('../models/contact')
/**
 * 
 * @param {any} req
 * @param {any} res
 * @param {any} next
 * {
 *  user_mac: "XXX",
 *  
 * }
 */
exports.createUser = async (req, res, next) => {
    try {
        var mac_check = req.body.user_mac;
        const existing_user = await User.findOne({
            user_mac: mac_check
        })
        if (!existing_user) {
            const new_user = await User.create(req.body)
            return res.status(200)
                .json({
                    success: true,
                    user_created: new_user
                })
        }
        return res.status(200).json({
            success: false,
            message: 'User already exists',
            existing_user: existing_user
        })

    } catch (err) {
        console.log(err.stack);
        res.status(400)
            .json({
                success: false,
                error: err.stack
            })
    }
}