# Registration
Registration and Authentication / Authorization Service

# Prerequisites
- PostgreSQL 12.2
    - For Windows you can install from [EnterpriseDB](https://www.enterprisedb.com/downloads/postgres-postgresql-downloads)
    
# Tools
- [IntelliJ 2019.3.3 Community Edition](https://www.jetbrains.com/idea/download)
- [Checkstyle-IDEA](https://plugins.jetbrains.com/plugin/1065-checkstyle-idea)
   
# How to build
You have to set an environment var for your local postgres db, like this:
```sh
DB_ADDRESS="jdbc:postgresql://localhost/postgres"; export DB_ADDRESS
DB_USER="postgres"; export DB_USER
DB_PASSWORD="postgres"; export DB_PASSWORD
```

You also need to set `AUTH_SECRET` in your environment, like this:
```sh
AUTH_SECRET="some random characters"; export AUTH_SECRET
```

# How to run

# Data Model
![](images/data_model.png)

# REST Users controller API Usage

### Create User Request
```http
POST http://localhost:8080/users HTTP/1.1
Content-Type: application/json
{
    "email": "John.Doe@gmail.com",
    "firstName": "John",
    "lastName": "Doe",
    "password": "test"
}
```
### Create User Response
```
HTTP/1.1 201
         226 IM Used (if already exists)
```
### Login Request
```http
POST http://localhost:8080/users/login HTTP/1.1
Content-Type: application/json
{
    "email": "John.Doe@gmail.com",
    "password": "test"
}
```
### Login Response
```http
HTTP/1.1 200
Content-Type: application/json
{ "token": "eyJhbGciOiJIUzI1NiJ9..."}
```
### Get User Request
```http
GET http://localhost:8080/users/ HTTP/1.1
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```
### Get User Response
```http
HTTP/1.1 200
Content-Type: application/json

{
    "firstName":"John",
    "lastName":"Doe",
    "email":"John.Doe@gmail.com",
    "password":null
}
```
### Update User Request
```http
PUT http://localhost:8080/users/ HTTP/1.1
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
    "firstName": "Johny",
    "lastName": "Doe"
}
```
### Update User Response
```http
HTTP/1.1 200
```
### Update Password
```http
PUT http://localhost:8080/users/password HTTP/1.1
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
    "oldPassword": "test",
    "newPassword": "Test"
}
```
### Update Password Response
```http
HTTP/1.1 200 
```
> **Note:** After this call, the token will not be valid.

### Logout Request
```http
PUT http://localhost:8080/users/logout HTTP/1.1
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```
### Logout Response
```
HTTP/1.1 200
```
> **Note:** After this call, the token will not be valid.


### Check Role membership for logged in user
```http
GET http://localhost:8080/users/role/ROLE_USER HTTP/1.1
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```
### Check Role membership Response
```
HTTP/1.1 200
```
Success
```
### Add Role to a User
```http
POST http://localhost:8080/users/role HTTP/1.1
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

Content-Type: application/json
{
	"email": "John.Doe@gmail.com",
	"roleName": "ROLE_USER"
}
```
### Add Role to a User Response
```
HTTP/1.1 200
```
Success

> **Note:** User needs to have ROLE_ADMIN to add role to any user.

### Delete Role from a User
```http
DELETE http://localhost:8080/users/role HTTP/1.1
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

Content-Type: application/json
{
	"email": "John.Doe@gmail.com",
	"roleName": "ROLE_USER"
}
```
### Delete Role from a User Response
```
HTTP/1.1 200
```
Success

> **Note:** User needs to have ROLE_ADMIN to delete role from any user.
