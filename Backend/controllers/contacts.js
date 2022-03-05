const contact = require('../models/contact');
const Contact = require('../models/contact')

//insert or update existing key count
exports.insertMAC = async (req,res,next) => {
    try{
        const {user_mac,date,new_contacts} = req.body
        const user = await Contact.findOne({user_mac});
        if(!user){
            const contNew = await Contact.create({user_mac: user_mac,contacts: [{date: date,contacts:new_contacts}]});
            return res.status(200).json({
                success: true,
                message:"User created, contacts updated",
                user: contNew,
                });
        }
        console.log(new_contacts)
        let list = user.contacts
        let temp = []
        for(let i = 0;i<list.length;i++){
            if(list[i].date == date){
                console.log("Date Found",new_contacts,list[i].contacts)
                list[i].contacts = list[i].contacts.concat(new_contacts)
                const updatedUser = await Contact.findByIdAndUpdate(user._id,{contacts:list},{
                    new: true,
                    newValidators: true
                });
                return res.status(200).json({
                    success: true,
                    message:"Date Found, MACs added!",
                    user: updatedUser
                    });
            }
        }
        list.push({date: date,contacts:new_contacts})
        const updatedUser = await Contact.findByIdAndUpdate(user._id,{contacts:list},{
            new: true,
            newValidators: true
        });
        return res.status(200).json({
            success: true,
            message:"New Date added",
            user: updatedUser
            });
    }catch(err){
        console.log(err.stack);
        res.status(400)
        .json({
            success: false,
            error: err.stack
        })
    }
}

function checkAndSort(qList, keyIndex){
    var keyCurr = qList[keyIndex].key;
    var countCurr = qList[keyIndex].count;
    if(keyIndex-1 >= 0){
        var k = keyIndex-1;
        while(k>=0 && countCurr < qList[k].count){
            k=k-1;
        }
        if(k<0){
            qList.splice(keyIndex,1);
            qList.splice(0,0,{"key":keyCurr,"count":countCurr});
            console.log(qList[0])
        }else if(!(countCurr > qList[k].count)){
            qList.splice(keyIndex,1);
            qList.splice(k+1,0,{"key":keyCurr,"count":countCurr});
            console.log(qList[k+1]);
        }
    }
    
    return qList;
}