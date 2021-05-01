# Simple Log Book
Day mode | Night mode

## Used libraries 
1. Navigation architecture
1. View binding
1. Dagger hilt
1. Coil/Glide for image loading
1. View model and Live Data
1. Some material custom theme

## How to use it also my json is there in project
1. Clone this repository
1. Create project in firebase
1. Enable google sign in authentication
1. Connect repository with the firebase

## App Flow
When the app is installed for thr very first time it will ask you to login or skip if you opt to login you
will be able to use across multiple devices and there will be a skip button if you dont want to opt in for this
feature you will be able to use app on same device and your data will be available in the same device even you
uninstall and install again however in user stories you wanted a toggle but there is no way to store data without
any unique id so i used google signin and device id for it and opened a screen instead of toggle.

If you skip or singin to google you will be redirected home screen where you can add log book and there is a delete
button in grid to delete a log book.

After adding a logbook you can just click on logbook to see entries and there you can add entries and see entries and
also there will be move icon on grid to move entries and when you lick on entriy details you can add pictures drawing
title and desription therewill be a pdf and delete option as well and you can also edit details.


 Please note i have used Firestore, Firebase Authentication, Firebase Storage as database because you had a requirement
 for cloud storage.