# BBoxapi-library

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/a7b50f5ddefe4ea2b888354fd0e9d59a)](https://www.codacy.com/app/gun95/bboxapi-library?utm_source=github.com&utm_medium=referral&utm_content=BboxLab/bboxapi-library&utm_campaign=badger)
[![Download](https://api.bintray.com/packages/bboxlab/maven/bboxapi-library/images/download.svg) ](https://bintray.com/bboxlab/maven/bboxapi-library/_latestVersion)
[![License](http://img.shields.io/:license-mit-blue.svg)](LICENSE.md)



This is the basic SDK for your own application if you want to make use of BboxAPI services from Bbox Miami. Don't forget to ask credentials (AppID/AppSecret) via https://dev.bouyguestelecom.fr/dev/?page_id=51

## Getting Started

1 -jar file
clone the depot and build
add **bboxapi-library-1.2-release.aar** to your lib folder of your project
add this line to your gradle dependencies  
````
compile(name: 'bboxapi-library-1.2-release.aar', ext: 'aar')
````
2 -maven
````
<dependency>
  <groupId>fr.bouyguestelecom.dev</groupId>
  <artifactId>bboxapi</artifactId>
  <version>1.2</version>
  <type>pom</type>
</dependency>
````
3 -jcenter
````
compile 'fr.bouyguestelecom.dev:bboxapi:1.2'
````
### Prerequisites

you need *app_id* and *app_secret* for some request
if you don't have, you can get one at :
https://dev.bouyguestelecom.fr/dev/?page_id=51

## Example

Find bbox Automatically :
```java
MyBboxManager bboxManager = new MyBboxManager();
MyBbox mBbox;

bboxManager.startLookingForBbox(context, new MyBboxManager.CallbackBboxFound() {
            @Override
            public void onResult(final MyBbox bboxFound) {
                // When we find our Bbox, we stopped looking for other Bbox.
                bboxManager.stopLookingForBbox();
                // We save our Bbox.
                mBbox = bboxFound;
                Log.i(TAG, "Bbox found: " + mBbox.getIp() + " macAdress: " + mBbox.getMacAddress());
            }
        });
```

get current channel : 
```java
Bbox.getInstance().getCurrentChannel(bbox.getIp(), getResources().getString(APP_ID),
getResources().getString(APP_SECRET),
new IBboxGetCurrentChannel() {
    @Override
    public void onResponse(final Channel channel) {
        System.out.println("status = " + channel.getMediaState());
        System.out.println("pos = " + channel.getPositionId());
        System.out.println("name = " + channel.getName());
        System.out.println("title = " + channel.getMediaTitle());
    }
    @Override
    public void onFailure(Request request, int errorCode) {
        Log.i("notif", "Get current channel failed");
    }
});
```

## Authors

* **Davinh2306** - *Initial work* - [bboxapi-client-android](https://github.com/BboxLab/bboxapi-client-android)

* **Gun95** - *update*


