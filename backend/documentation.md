# Documentation 
                             front end       back end
Registration  -              done            done

Login -                      done            done

Route protection -           done             done

Dashboard -                     

Search w/filters            seems done      done

status checkboxes

trial detail page

save, status, follow up feature

notes (CRUD)

reports

Change alerts

===========================================================

Architextural flow

browser (port 4200) --> json over http w/jwt token ---> spring boot API port 8080
spring boot then goes to mySQL db/clinicaltrials.gov API/zip code lookups zippopotam.us

-- Browser doesn't talk with api services, this way spring can handle any errors, validation, and caching allowing for 
good UX on the front end

# Backend

-- controller -> service -> repository -> mySQL
* the controller receives the http requests, runs validation and returns a response
* controller calls upon the service for business logic
* repository classes write to the database
* entity package contains the objects
* DTO is the shape of data going in and out of the API
* wraps outside webservices


# Packages

* Security config - stateless (doesn't have sessions), determines who is allowed to call what - lists
public routes, everything else is protected. CORS allows the front end and back end to talk to one another.
Password encoder bcrypt bean is included for password hashing

* Security - JWT service creates a signed token at login that verifies tokens for later requests, expires 
automatically after 1 hour. JWT auth filter runs on every request, it confirms the token is valid and tells
spring who the caller is, if the token is bad or missing the call stays anonymous.

* Controller - Health controller - for early development testing. Auth controller for user auth purposes.
User controller for users that are logged in. Study controller works with searches and studies.

* Service - UserService - allows users to register and login. StudySearchService - takes the user search request
to clinicaltrials.gov api and makes the api call, then it takes a large json response and maps it to smaller DTO's

* Client - ZipGeocodingService - converts zip codes to latitude and longitudes, saves answers, quick timeout to not impede UX. 
ClinicalTrialClient - builds the url for clinicaltrials.gov, uses filter than the user has used, also has a quick timeout.
GeoPoint - a longitude/latitude record

* Repository - UserRepository - uses the CRUD type methods from JPARepository, also adds two methods to find by email.
RecruitmentStatusRepository - adds two additional methods as well

* Entity - Base entity for encapsulation and inheritance as required for this project, allows other entities to inherit
common attributes and methods. User entity is included to house the user object. RecruitmentStatus to track the recruitment
status of trials. Tracking status and saved study entities serve as objects to track statuses and to house saved trial entities.

* DTO - includes records for multiple data types

* Exception - includes custom exception types for emails that already exist on user sign ups, invalid login credentials,
invalid searches and external service errors, will the global exception handler used as a bottom level exception.


# Flow of the app

User registration
* Angular sends a POST /register request with email password and first name (don't want to collect too much data because of 
potentially sensitive medical information). Auth service in Angular stores the token, user and token expiration in
local storage, user is automatically logged out when the token expires. The interceptor adds the 'Auth:Bearer' header to 
every API request. JWTAuth filter validates the token, controllers read who it is from the authentication object, not
directly from the browser


User login and sensitive requests
* POST /login returns an AuthResponse containing a JWT. Angular stores the token inside its AuthService along with the 
user and when the token expires. Interceptor and AuthFilter works the same as in registration.



Search
* All the filters are optional, just like on most websites. Query params are taken from clinicaltrials.gov website.


Database
* Users table - houses user information created by the V1 file in db.migration. recruitment_statuses table - created
by V2 file in db.migration, contains information reguarding the recruitment status of the trial. 
* DONOT CHANGE the db.migration files it will mess things up




# Paths

* GET /api/ping - development testing
* POST /api/auth/register - account creation
* POST /api/auth/login - validates user gives them a JWT token
* GET /api/users/me - returns the current user, must be logged in to access
* GET /api/studies/statuses - checkbox options from SQL
* GET /api/studies/search - actual search feature for trials





### NEED TO BUILD
* search page ---- done
* trial detail page
* saved trials
* notes
* reports
* tests
* login to save trials -- done
* login redirect ----- done
* plain language summaries - trial summary w/word lookup??


## Potential add ons
* change alerts
* email sms notifications on change
* trial comparisons
* alerts for new posted trials/studies that match a users condition/saved demographics



# Security note for saved trials
The server needs to verify the user from their token, not the users ID sent by the browser. 
This way someone cannot change the URL to get another persons data.