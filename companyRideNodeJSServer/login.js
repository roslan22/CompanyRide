var userModel = require('./userModel');
var generics  = require("./genericFunctions");

exports.authorize = function(req, res)
{
	var email = req.params.name;
	var pass = req.params.pass;

	console.log("Login with email " + email);

	userModel.findOne({'professionalEmail':email}).select('_id userProfileId password').exec(function callback(err, doc)
	{
		if(err)
		{
			console.log("Error retrieving user: " +  err.message);
			return res.status(500).send(generics.wrappedResponse("error", 'Server error, cannot load user document'));
		}
		else
		{
		   if(!doc)
		   {
				 console.log("No document found with email: ", email);
				 return res.status(400).send(generics.wrappedResponse("error",'Bad username or password '));
		   }
		   else if(doc.password !== pass)
		   {
				console.log("Bad password for email: ", email);
				return res.status(400).send(generics.wrappedResponse("error",'Bad username or password '));
		   }
		   else
		   {
				 delete doc._doc.password;
				 //delete doc['password']  //deletng password before sending back to user
				 console.log("document is: ", doc);
				 return res.status(200).send(generics.wrappedResponse("success", "Successfull login", doc));
		   }
		}
	});
}
