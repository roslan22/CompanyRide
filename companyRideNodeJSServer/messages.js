var userProfileModel = require("./userProfileModel");

//message is json with all message details
exports.sendMessageToUserProfile = function(message, userProfileId, callback)
{
    userProfileModel.findOneAndUpdate( {_id: userProfileId},
       { $push: {"messages": message}},
        { safe: true, new: true},
        function(err, doc)
        {
            if (err || !doc)
                callback("error");
            else
            {
                console.log(doc);
                callback(null);
            }
        }
    );
}

exports.removeMessageFromUserProfile = function(userProfileId, rideId, type, callback)
{
    userProfileModel.findOneAndUpdate({"_id" : userProfileId},
        {$pull: {'messages' : {"rideId": rideId ,"type": type}}},
        { safe: true, new: true},
        function(err, doc)
        {
            if(err || !doc) callback ("error");
            else callback(null, doc.fullName);
        }
    );
}
