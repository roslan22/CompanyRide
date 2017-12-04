# CompanyRide
Ride sharing android application + server side on nodeJs + matching algorithm in Java+MongoDB

CompanyRide is an application that helps people to share a ride to work or home or anywhere else. We will match potential companions for a ride, provide an easy interface to manage rides and control the current ride.

In CompanyRide application users can both propose a ride (be a driver) and ask for a ride (be a hitcher). As all rides should be tied to some date and hour all user’s rides are managed like a calendar with additional location data. Ride can be one-time or reoccurring (as a calendar event).
Main process:
1) Hitchers adds their desired rides (source and destination). Drivers adds their planned rides. 2) CompanyRide application will try to match relevant hitchers for a ride.
3) Matched potential hitcher profile will be introduced to the ride driver, and he/she will be asked to confirm or decline a potential hitcher. All the process will be 100% anonymously in way that hitchers will not see any decline message from a driver, like a match never happened.
4) If driver confirmed the hitcher, the hitcher will be introduced to the driver profile and now he/she will be asked to confirm or decline a ride with this person.
5) When a ride is confirmed by driver and hitcher(s) access to Ride console is opened.

Ride console
The ride console provides button controls for easy and simple communication between participants (E.g. “I’m at the point”, “I’ll be 5 min. late”, “Not coming” and so on). Users can also switch to a map view to find the meeting point. Ride Console facilitates safe driving, which is very important. Rating and badges
Both drivers and hitchers will gain points after participating in a ride. The points will be granted (or not) based on a short feedback and ride console log (e.g. did participator came at time).
But rating is boring. So, in addition to rating, we will distribute badges “Rider of the week”, “Interesting companion”, “Never Late” and so on...


