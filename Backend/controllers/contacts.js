const contact = require('../models/contact')

//insert or update existing key count
exports.insertMAC = async (req,res,next) => {
    try{
        const {user_mac,user_contacts} = req.body

        
        return res.status(200).json({
            success: true,
            message:"Successfully key inserted/updated",
            user: user_mac,
            contacts: user_contacts
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
        while(k>=0 && countCurr > qList[k].count){
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