var userProfileModel = require('./userProfileModel');
var generics = require('./genericFunctions');

exports.getUserProfileById = function(req, res, selectedFiels )
{
	var id = req.params.id;
	console.log("Fetching user profile " + id);

	userProfileModel.findById(id).select(selectedFiels).exec(
	function callback(err, doc)
	{
		if(err)
		{
			console.log("Problem fetching userProfile ", id);
			return res.status(500).send(generics.wrappedResponse("error",'Problem fetching userProfile: ' + err.message));
		}
		if (!doc)
		{
			return res.status(400).send(generics.wrappedResponse("error", 'UserProfile with id ' + id + " wasn't found"));
		}
		else
		{
			 return res.status(200).send(generics.wrappedResponse("success", "UserProfile successfully fetched",doc));
		}
	});
}

//not available from outside (not extern)
//is called only after professional email was validated
exports.createNewUserProfile = function(userProfileDocument, callback)
{
	console.log("create new user profile " + userProfileDocument);
	var newUserProfileDocument  = new userProfileModel(userProfileDocument);
	newUserProfileDocument.save(
		function(err, doc)
		{
			if (err && !doc)
			{
				console.log("Failed insert document to userProfiles:" + userProfileDocument);
				callback("error");
			}
			else callback(null,doc);
		}
	)
}


//only specialRequests[] or status
//other parameters should be updated through user document
exports.updateUserProfileById = function(req, res)
{
	var id = req.params.id;
	console.log("Update user profile for id: " + id);

    var newInfo = {};
	if (req.body.specialRequests) newInfo.specialRequests = req.body.specialRequests;
	if (req.body.status) newInfo.status = req.body.status;

    if (newInfo.specialRequests || newInfo.status)
    {
    	userProfileModel.findOneAndUpdate({_id: id}, {$set: newInfo},
        function(err, doc)
    	{
    		if(err)
    		{
    			console.log("Problem fetching user profile ", id);
    			return res.status(500).send(generics.wrappedResponse("error",'Problem fetching user profile document: ' + err.message));
    		}
			if (!doc)
			{
				return res.status(400).send(generics.wrappedResponse("error", 'User profile with id ' + id + " wasn't found"));
			}
    		else
    		{
                console.log(doc);
    			 return res.status(200).send(generics.wrappedResponse("success","Document was successfully updated", doc));
    		}
    	});
    }
    else
    {
        return res.status(400).send(generics.wrappedResponse("error", "no information to update received"));
    }
}


exports.AddUserToBlockedList = function(req, res)
{
	var id = req.params.id;
	var userProfileToBlock =  req.params.userProfileToBlock;
	console.log("Update user profile id: " + id + " insert from blocked list: " + userProfileToBlock);

	userProfileModel.findByIdAndUpdate(id,{$push: {"blockedUsers": userProfileToBlock}},
    {safe: true, upsert: true},
	function(err, doc)
	{
		if(err)
		{
			console.log("Problem fetching user profile ", id);
			return res.status(500).send(generics.wrappedResponse("error",'Problem fetching user profile document: ' + err.message));
		}
		if (!doc)
		{
			return res.status(400).send(generics.wrappedResponse("error", 'User profile with id ' + id + " wasn't found"));
		}
		else
		{
			console.log(doc);
			return res.status(200).send(generics.wrappedResponse("success","Document was successfully updated", doc.blockedUsers));
		}
	});
}



exports.RemoveUserFromBlockedList = function(req, res)
{
	var id = req.params.id;
	var userProfileToUnBlock =  req.params.userProfileToUnBlock;
	console.log("Update user profile id: " + id + " remove from blocked list: " + userProfileToUnBlock);

	userProfileModel.findByIdAndUpdate(id,{$pull: {"blockedUsers": userProfileToUnBlock}},
    {safe: true, upsert: true},
	function(err, doc)
	{
		if(err)
		{
			console.log("Problem fetching user profile ", id);
			return res.status(500).send(generics.wrappedResponse("error",'Problem fetching user profile document: ' + err.message));
		}
		if (!doc)
		{
			return res.status(400).send(generics.wrappedResponse("error", 'User profile with id ' + id + " wasn't found"));
		}
		else
		{
			console.log(doc);
			return res.status(200).send(generics.wrappedResponse("success","Document was successfully updated", doc.blockedUsers));
		}
	});
}

exports.RemoveMessage = function(req, res)
{
	var id = req.params.id;
	var messageToDelete =  req.body;
	console.log("Update user profile id: " + id + " remove message: " + messageToDelete);

	userProfileModel.findByIdAndUpdate(id,{$pull: {"messages": messageToDelete}},
    {safe: true, upsert: true},
	function(err, doc)
	{
		if(err)
		{
			console.log("Problem fetching user profile ", id);
			return res.status(500).send(generics.wrappedResponse("error",'Problem fetching user profile document: ' + err.message));
		}
		if (!doc)
		{
			return res.status(400).send(generics.wrappedResponse("error", 'User profile with id ' + id + " wasn't found"));
		}
		else
		{
			console.log(doc);
			return res.status(200).send(generics.wrappedResponse("success","Document was successfully updated", doc.messages));
		}
	});
}
