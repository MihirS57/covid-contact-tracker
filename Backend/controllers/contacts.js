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
        //console.log(user_2)
        console.log(new_contacts)
        let list = user.contacts
        let ind = 0
        for(let item of list){
            if(item.date == date){
                new_contacts.forEach(element => {
                    ind = item.contacts.findIndex(x => x.contact_mac == element.contact_mac)
                    console.log("Contact Found: ",item.contacts[ind])
                    if(ind != -1){
                        if(element.rmmi > item.contacts[ind].rmmi){
                            item.contacts[ind].rmmi = element.rmmi
                        }
                    }else{
                        item.contacts.push(element)
                    }
                });
                const updatedUser = await Contact.findByIdAndUpdate(user._id,{contacts:list},{
                    new: true,
                    newValidators: true
                });
                new_contacts.forEach(element => {
                    updateInfoOnAddedContacts(user_mac,element.contact_mac,date,element.rmmi)
                })
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

const updateInfoOnAddedContacts = async (host_mac, u_mac, contact_date, contact_rmmi) => {
    try{
        console.log(u_mac,"->",host_mac," updating...")
        const user_2 = await Contact.findOne({user_mac:u_mac});
        console.log("User",user_2)
        if(!user_2){
            const contNew = await Contact.create(
                {
                    user_mac: u_mac,
                    contacts: [
                        {
                            date: contact_date,
                            contacts:[
                                {
                                    contact_mac: host_mac,
                                    rmmi: contact_rmmi
                                }
                            ]
                        }
                    ]
            });
            console.log("New user created",contNew)
            return;
        }
        let list = user_2.contacts
        let ind = 0
        for(let item of list){
            if(item.date == contact_date){
                ind = item.contacts.findIndex(x => x.contact_mac == host_mac)
                console.log("Contact Found: ",user_2)
                if(ind != -1){
                    if(contact_rmmi > item.contacts[ind].rmmi){
                        item.contacts[ind].rmmi = contact_rmmi
                    }
                }else{
                    item.contacts.push({contact_mac: host_mac, rmmi: contact_rmmi})
                }
                
                const updatedUser = await Contact.findByIdAndUpdate(user_2._id,{contacts:list},{
                    new: true,
                    newValidators: true
                });
                console.log("Updated User: ",updatedUser)
                return;
            }
        }
        list.push({date: contact_date,contacts:{contact_mac: host_mac, rmmi: contact_rmmi}})
        const updatedUser = await Contact.findByIdAndUpdate(user._id,{contacts:list},{
            new: true,
            newValidators: true
        });
        console.log("Updated User: ",updatedUser)
        return;
    }catch(err){
        console.log("Err occurred! ",err.message)
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