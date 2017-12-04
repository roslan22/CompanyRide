
var rides = require("./rides");
var rideRequestModel = require ("./rideRequestModel");
var rideRequests = require("./rideRequests");
var genericFunctions = require("./genericFunctions");
var enums = require("./enums");


function performRequiredActionOnRideRequest(rideReqId, req, freeRangesOfRideRequest, callback)
{
  if (freeRangesOfRideRequest.length > 0)
  {
      updateRideReqWithFirstRange(req._id, freeRangesOfRideRequest[0],
      function (err)
      {
          if (err)
          {
              console.log("Error while updating ride request" + rideReqId + " with new range" + err);
              callback("Error while updating ride request" + rideReqId + " with new range");
          }
          else
          {
              if (freeRangesOfRideRequest[1])
              {
                 insertCopyOfRideRequestWithSecondRange(req, freeRangesOfRideRequest[1],
                 function(err)
                 {
                    if (err)
                    {
                        console.log("Error while inserting  new ride request split" + err);
                        callback("Error while inserting  new ride request split");
                    }
                    else {
                      callback(null);
                    }
                 });
              }
              else callback(null);
          }
      });
  }
  else
  {
        rideRequestModel.findOneAndRemove({"_id": rideReqId},
        function(err, doc)
        {
            if (err)
            {
                console.log("Failed to remove ride request " +  rideReqId + " " +  err);
                callback("Failed to remove ride request " +  rideReqId);
            }
            else callback(null);
        });
  }
}

exports.splitRideRequestForPotentialRide = function(potRideChanges, callback)
{
    var weekday   = potRideChanges.weekday;
    var req       = potRideChanges.origReq;
    var rideReqId = req._id;
    var rideReqRange = {startDate: req.startDate, stopDate: req.stopDate};
    var potRideRange = {startDate: potRideChanges.startDate, stopDate: potRideChanges.stopDate};

    var freeRangesOfRideRequest = [];
    if(req.eventType === 'weekly')
      freeRangesOfRideRequest = getRideRequestsRangesThatAreNotInRide(rideReqRange, potRideRange, weekday);
    console.log( "freeRangesOfRideRequest: " +  JSON.stringify(freeRangesOfRideRequest));
    performRequiredActionOnRideRequest(rideReqId, req, freeRangesOfRideRequest,
      function(err){
        if(err){
          callback(err);
        }
        else{
          var newReq = JSON.parse(JSON.stringify(req));
          delete newReq["_id"];
          newReq.startDate = potRideChanges.startDate;
          newReq.stopDate = potRideChanges.stopDate;
          newReq.eventType = 'one-time';
          newReq.to       = potRideChanges.to;
          newReq.preferredRideTime.fromHour = potRideChanges.fromHour;
          newReq.preferredRideTime.toHour = potRideChanges.toHour;
          console.log("new request will be inserted: " + JSON.stringify(newReq));
          rideRequests.addNewRideRequestFromJSON(newReq, callback);
        }
      }
    );
}

//this function doesn't check whether 2 documents are connected, but the range only
exports.splitRideRequestAfterRideCreation = function(rideId, rideReqId, weekday, callback)
{
    rideRequests.getRideReqFromDB(rideReqId, function(err, docRideReq)
    {
        if (!err && docRideReq)
        {
              rides.getRideFromDB(rideId, function (err, docRide)
              {
                  if (!err && docRide)
                  {

                      var rideReqRange = {startDate: docRideReq.startDate, stopDate: docRideReq.stopDate};
                      var rideRange = {startDate: docRide.startDate, stopDate: docRide.stopDate};

                      var freeRangesOfRideRequest = getRideRequestsRangesThatAreNotInRide(rideReqRange, rideRange, weekday);
                          console.log( "freeRangesOfRideRequest: " +  JSON.stringify(freeRangesOfRideRequest));

                      performRequiredActionOnRideRequest(rideReqId, docRideReq, freeRangesOfRideRequest, callback);
                  }
                  else
                  {
                      console.log("Operation splitRideRequestAfterRideCreation can't be completed" + err);
                      callback("Operation splitRideRequestAfterRideCreation can't be completed");
                  }
              });
        }
        else
        {
          console.log("Operation splitRideRequestAfterRideCreation can't be completed" + err);
          callback("Operation splitRideRequestAfterRideCreation can't be completed");
        }
    });
}

function updateRideReqWithFirstRange(rideReqId, dateRange, callback)
{
    var newInfo =
    {
        startDate: dateRange.startDate,
        stopDate: dateRange.stopDate,
        status: "unmatched"
    };

    console.log("updateRideReq" + rideReqId + "WithFirstRange: newInfo:  ", JSON.stringify(newInfo));

    rideRequestModel.findOneAndUpdate({_id: rideReqId}, {$set: newInfo},
    function(err, doc)
    {
        if(err || !doc)
        {
            console.log("Error updating ride request " + rideReqId + "with new date range: " + newInfo + err);
            callback("Error updating ride request " + rideReqId + "with new date range: " + newInfo);
        }
        else  callback(null);
    });
}


function insertCopyOfRideRequestWithSecondRange(rideReqDoc, dateRange, callback)
{
    var newRideRequest = JSON.parse(JSON.stringify(rideReqDoc));
   delete newRideRequest["_id"];
   newRideRequest.startDate = dateRange.startDate;
   newRideRequest.stopDate = dateRange.stopDate;
   newRideRequest.status = "unmatched";

    var newRideRequestDocument  = new rideRequestModel(newRideRequest);
    newRideRequestDocument.save(
    function(err, doc)
    {
        if (err && !doc)
        {
            console.log("Failed insert new range ride request document :" + rideReqDoc + err);
            callback("Failed insert new range ride request document ");
        }
        else callback(null,doc);
    });
}



//range object should have form
//{startDate : startDateVal, stopDate: stopDateVal}
//all dates in date format
//function will return an array of  ride request ranges which are not overlaping with ride
function getRideRequestsRangesThatAreNotInRide(rideRequestRange, rideRange, weekday)
{
  console.log(     "ride req range"        );
  console.log(JSON.stringify(rideRequestRange));
  console.log(     "ride range"            );
  console.log(JSON.stringify(rideRange));

   var rangesArr = [];

   //if rideRequest.startDate < ride.startDate  ==> create first range
   if(compareDates(rideRequestRange.startDate, rideRange.startDate) === -1){
     console.log("Ride > Request start date");
     rangesArr.push(range(rideRequestRange.startDate, genericFunctions.dateMinusDay(rideRange.startDate)));
   }

    //if ride.stopDate < rideRequest.stopDate  ===> create second range
    if (compareDates(rideRange.stopDate, rideRequestRange.stopDate) === -1){
      console.log("Ride < Request stop date");
      rangesArr.push(range(genericFunctions.getFirstWeekDayDateAfterDate(rideRange.stopDate,weekday), rideRequestRange.stopDate));
    }

      if (rangesArr.length === 0  &&
          (compareDates(rideRange.stopDate, rideRequestRange.stopDate) === 0) &&
          (compareDates(rideRequestRange.startDate, rideRange.startDate) === 0))
            console.log("bingo!!!");
      else if ((compareDates(rideRequestRange.startDate, rideRange.startDate) === 1) ||
               (compareDates(rideRange.stopDate, rideRequestRange.stopDate) === 1))
            console.log("invalid state!!! Ride req range: " +  JSON.stringify(rideRequestRange) + " Ride range: " +  JSON.stringify(rideRange));

        console.log( "freeRangesOfRideRequest: " + rangesArr.toString());
      return rangesArr;
}


// return 0 - dates equals
// 1 date1 > date2
//-1 date1 < date2
function compareDates (date1, date2)
{
  newDate1 = new Date(date1);
  newDate1.setHours(0,0,0,0);
  newDate2 = new Date(date2);
  newDate2.setHours(0,0,0,0);
  var t1 =  newDate1.getTime();
  var t2 = newDate2.getTime();
  if (t1 === t2) return 0;
  else if (t1 > t2) return 1;
  else return -1;
}

function range(start, stop)
{
    return  {"startDate": start, "stopDate": stop};
}

// var millisecondsInWeek = 604800000;
//
// function datePlusWeek(date)
// {
//   return genericFunctions.getUTCNewDateFromMilliseconds(date.getTime() + millisecondsInWeek);
// }
