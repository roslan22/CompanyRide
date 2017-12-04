var express = require('express');
var multer  = require('multer');
var bodyParser = require('body-parser');
var users = require('./users');
var profiles = require('./userProfiles');
var rideRequests = require('./rideRequests');
var rides = require('./rides');
var mongoConnect = require('./mongoConnect');
var login = require('./login');
var mail = require('./mail');
var rating = require('./rating');
var feedback = require('./feedback');
var generics = require('./genericFunctions');
var generalVars = require("./generalVars");

/*------------------SMTP Over-----------------------------*/

var app = express();
app.use(express.static(__dirname + '/public'));
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({extended: true}));

app.get('/', function (req, res) {res.send('Company Ride Application')});

app.post('/user/',            users.createNewUser);
app.get('/user/:id',          users.getUserById);
app.put('/user/:id',          users.updateUserById);
app.post('/user/token/:id/:token', users.setUserToken);
app.post('/user/picture/:id', [ multer({
      dest: __dirname + '/public/profilePictures',
      rename: function(fieldname, filename) {
          return filename;
      },
      onFileUploadStart: function (file, req, res) {
        console.log(file.fieldname + ' is starting ...')
      }
    }), function(req, res){
    console.log(req.body) // form fields
    console.log(req.files['picture']) // form files
    res.status(200).send(generics.wrappedResponse("success","Picture was successfully updated"));
}]);

app.get('/userProfile/full/:id',
function(req, res){profiles.getUserProfileById(req, res, '-__v -_id')});
app.get('/userProfile/short/:id',
function(req, res){profiles.getUserProfileById(req, res, '-__v -_id -messages -blockedUsers -status')});
app.get('/userProfile/minimal/:id',
function(req, res){profiles.getUserProfileById(req, res, 'fullName occupationTitle')});
app.put('/userProfile/:id',profiles.updateUserProfileById);
app.put('/userProfile/:id/block/:userProfileToBlock', profiles.AddUserToBlockedList);
app.put('/userProfile/:id/unblock/:userProfileToUnBlock',profiles.RemoveUserFromBlockedList);
app.put('/userProfile/:id/message/delete',profiles.RemoveMessage);

app.get('/rideRequest/:id',rideRequests.getRideRequestById);
app.get('/rideRequest/:userid/:month',rideRequests.getRideRequestsForMonth);
app.get('/rideRequest/potential/rides/:type/:id/:date',rideRequests.getRideRequestsPotentialRides);
app.post('/rideRequest/',rideRequests.addNewRideRequest);
app.put('/rideRequest/:id',rideRequests.updateRideRequest);
app.put('/rideRequest/onetime/:id',rideRequests.updateSplitRideRequest);
app.delete('/rideRequest/:id',rideRequests.removeRideRequestById);

app.get('/ride/:id',rides.getRideById);
app.get('/ride/:userid/:month',rides.getRidesForMonth);
app.put('/ride/:rideId/hitcher/:hitcherId/approve', rides.setHitcherApprovement);
app.put('/ride/:rideId/hitcher/:hitcherId/disapprove', rides.setHitcherDisapprovement);
app.put('/ride/:rideId/hitcher/:hitcherId/pickUpDropDetails/approve',
rides.setHitcherPickUpDropDetailsApprove);
app.put('/ride/:rideId/driver/:hitcherId',rides.setPickUpDropDetails);
app.put('/ride/:rideId/driver/:hitcherId/disapprove', rides.driverDisaproveHitcher);
app.put('/ride/:rideId/driver/:hitcherId/approve', rides.setPickUpDropDetails);
app.post('/ride/:rideId/hitcher/:hitcherId/messageFromDriver', rides.addMessageToHitcherReceivedMessages);
app.post('/ride/:rideId/hitcher/:hitcherId/messageForDriver', rides.addMessageToHitcherSentMessages);
app.delete('/ride/:rideId/hitcher/:hitcherId',rides.stopParticipateInRide);
app.delete('/ride/:rideId/driver',rides.removeRide);

app.get('/login/:name/:pass',login.authorize);
app.put('/:userId/:rideId/thanks', rating.thankDriver);
app.get('/verifyMail/:mail/:userId/:verification',mail.verifyUser);

app.put('/feedback/:category/id/:userId', feedback.sendEmailToUser);

console.log("Starting server...");
app.listen(generalVars.port,generalVars.host);
