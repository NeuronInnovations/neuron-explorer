
const urlParams = new URLSearchParams(window.location.search);
const lineson = urlParams.has('lineson');

const rightInfoPanel = document.getElementById('right-info-panel');
const rightInfoPanelBack = document.getElementById('right-info-panel-back');
const toughLogo = document.getElementById('toughlogo');
const toughMenu = document.getElementById('toughmenu');
const toughLogout = document.getElementById('toughlogout');
const toughLogin = document.getElementById('toughlogin');
const toughSignup = document.getElementById('toughsignup');
const toughHome = document.getElementById('toughhome');
const appbar = document.getElementById('app-bar');
function handleResize() {
    const rightInfoPanel = document.getElementById('right-info-panel');
    const rightInfoPanelBack = document.getElementById('right-info-panel-back');
    
    if (window.matchMedia("(max-width: 882px)").matches) {
        rightInfoPanel.style.display = 'none';
        rightInfoPanelBack.style.display = 'none';
        const toughLogo = document.getElementById('toughlogo');    } else {
        rightInfoPanel.style.display = 'block';
        rightInfoPanelBack.style.display = 'block';
    }
}

handleResize();

window.addEventListener('resize', handleResize);

function toggleToughLogoVisibility() {
    const rightInfoPanel = document.getElementById('right-info-panel');
    const rightInfoPanelBack = document.getElementById('right-info-panel-back');
    const toughLogo = document.getElementById('toughlogo');
    const toughMenu = document.getElementById('toughmenu');
    const toughHome = document.getElementById('toughhome');


    const rightInfoPanelVisible = rightInfoPanel && rightInfoPanel.style.display !== 'none';
    const rightInfoPanelBackVisible = rightInfoPanelBack && rightInfoPanelBack.style.display !== 'none';

    if (!rightInfoPanelVisible && !rightInfoPanelBackVisible) {
        toughLogo.style.display = 'block'; // Make toughlogo visible
        toughMenu.style.display = 'block';
        toughHome.style.display = 'block';
    } else {
        toughLogo.style.display = 'none'; // Make toughlogo invisible
        toughMenu.style.display = 'none';
        toughHome.style.display = 'none';
    }
}

toggleToughLogoVisibility();
document.getElementById('right-info-panel').addEventListener('change', toggleToughLogoVisibility);
document.getElementById('right-info-panel-back').addEventListener('change', toggleToughLogoVisibility);

const REFRESH_TIME = 30000;
const ANIMATIONS = {
    '*': { speed: 1 }
};

let mapLatitude = 52.1
let mapLongitude = -0.01;
let mapZoom = 7;
let mapBearing = 0;
let mapPitch = 0;

let useCoverageLayer = false;
let useGroundTrackLayer = false;
let useSensorArcLayer = false;

const globalEventSourcePointer = new EventSource("/emitDevices/");
globalEventSourcePointer.addEventListener('message', onMessageHandler, false);

const peerIdToXYX = {}

var g_SelectedDevice = "";

const mydeckgl = new deck.DeckGL({
    container: 'container',
    mapboxApiAccessToken: 'your-mapbox-access-token-here',
    mapStyle: 'mapbox://styles/neuron-innovations/cl9zpu0tj006t15phyzh44c2b',
    controller: { doubleClickZoom: false },

    initialViewState: {
        latitude: mapLatitude,
        longitude: mapLongitude,
        zoom: mapZoom,
        pitch: mapPitch,
        bearing: mapBearing,
        minZoom: 0,
        maxZoom: 12,
        minPitch: 40,
        maxPitch: 60
    }
});


function showFrontCard() {
    
    rightInfoPanel.style.display = 'block';
    flipCard('front');
    toughLogo.style.display = 'none';
    toughMenu.style.display = 'none';
    toughSignup.style.display = 'none';
    toughHome.style.display = 'none';
}

function showMap() {
    document.getElementById("right-info-panel").style.display = 'none';
    document.getElementById("right-info-panel-back").style.display = 'none';
    toughLogo.style.display = 'block';
    toughMenu.style.display = 'block';
    toughSignup.style.display = 'block';
    toughHome.style.display = 'block';

}

function flipCard(side) {

    if (side === 'front') {
        if (window.matchMedia("(max-width: 882px)").matches) {
       
            toughLogo.style.display = 'block';
            toughMenu.style.display = 'block';
            toughSignup.style.display = 'block';
            toughHome.style.display = 'block';
            
        } else {

        }

        document.getElementById("right-info-panel").style.transform = "rotateY(0deg)";
        document.getElementById("right-info-panel").style.zIndex = 8;
        document.getElementById("right-info-panel").scrollTop = 0

        document.getElementById("right-info-panel-back").style.transform = "rotateY(180deg)";
        document.getElementById("right-info-panel-back").style.zIndex = 1;

    } else if (side === 'back') {


        if (window.matchMedia("(max-width: 882px)").matches) {
            toughLogo.style.display = 'none';
            toughMenu.style.display = 'none';
            toughSignup.style.display = 'none';
            toughHome.style.display = 'none';
            rightInfoPanelBack.style.display = 'block';

        } else {

        }

        document.getElementById("right-info-panel").style.transform = "rotateY(180deg)";
        document.getElementById("right-info-panel").style.zIndex = 1;;

        if (document.getElementById("right-info-panel-back").style.transform == "rotateY(0deg)") {
            document.getElementById("right-info-panel-back").style.transform = "rotateY(-40deg)";
            setTimeout(() => {
                document.getElementById("right-info-panel-back").style.transform = "rotateY(0deg)";
              }, 300);
           
        } else {
            document.getElementById("right-info-panel-back").style.transform = "rotateY(0deg)";
        }
        document.getElementById("right-info-panel-back").style.zIndex = 8;
        document.getElementById("right-info-panel-back").scrollTop = 0
    } else {
        console.log("ERROR: Unknown side: " + side);
    }
}

flipCard('front')

function addRemoveSensorArcLayer() {
    var e = document.getElementById("sensor-arc-layer");
    !useSensorArcLayer ? e.classList.add("enabled") : e.classList.remove("enabled");
    useSensorArcLayer = !useSensorArcLayer;
}

function addRemoveGroundTrackLayer() {
    const e = document.getElementById("ground-track-layer");
    !useGroundTrackLayer ? e.classList.add("enabled") : e.classList.remove("enabled");
    useGroundTrackLayer = !useGroundTrackLayer;
}

function addRemoveCoverageLayer() {
    const e = document.getElementById("coverage-layer");
    !useCoverageLayer ? e.classList.add("enabled") : e.classList.remove("enabled");
    useCoverageLayer = !useCoverageLayer;
}

function toPitchZero() {
    let mapPitch = 0;
    mydeckgl.setProps({
        initialViewState: {
            latitude: mapLatitude,
            longitude: mapLongitude,
            zoom: mapZoom,
            bearing: mapBearing,
            pitch: mapPitch
        }
    });
}

function toPitchNinety() {
    mapPitch = 60;
    mydeckgl.setProps({
        initialViewState: {
            latitude: mapLatitude,
            longitude: mapLongitude,
            zoom: mapZoom,
            bearing: mapBearing,
            pitch: mapPitch
        }
    });
}
function rotateThirdy() {
    mapBearing = mapBearing + 30;
    mydeckgl.setProps({
        initialViewState: {
            latitude: mapLatitude,
            longitude: mapLongitude,
            zoom: mapZoom,
            bearing: mapBearing,
            pitch: mapPitch
        }
    });
}

let hoveredIndex = null;
function onMessageHandler(event) {
    data = JSON.parse(event.data);

    const last4ToFullAddress = {};
        data.forEach(d => {
            const last4 = d.address.slice(-4);
            last4ToFullAddress[last4] = d.address;
    });
    console.log(data);

    data = data.filter(m => m.isValid);

    getStats();

    data.map(d => peerIdToXYX[d.address] = [d.lon, d.lat, d.alt])

    const deviceTrackLayer = new deck.ScatterplotLayer({
        id: 'deviceTrackLayer',
        data,
        pickable: false,

        stroked: true,
        filled: true,
        radiusMinPixels: 8,
        radiusMaxPixels: 30,
        lineWidthMinPixels: 4,
        sizeScale: 10000,
        getPosition: d => [d.lon, d.lat, d.alt],
        getFillColor: d => [0, 230, 255, secondsToAlpha(d.lastStdOutMessageTimestamp >= d.lastStdInMessageTimestamp ? d.lastStdOutMessageTimestamp : d.lastStdInMessageTimestamp)],
        getLineColor: d => [0, 0,   0,   secondsToAlpha(d.lastStdOutMessageTimestamp >= d.lastStdInMessageTimestamp ? d.lastStdOutMessageTimestamp : d.lastStdInMessageTimestamp)],
    });

    const iconLayer = new deck.IconLayer({
        id: 'IconLayer',
        data: data,
        pickable: true,
        autoHighlight: true,

       getHighlightColor: d => d.address === g_SelectedDevice.address ? [0, 230, 255, 250]: [33,33, 33, 33],
       
        onClick: (info, event) =>{ 
            g_SelectedDevice = data[ info.index ]
            peerToCard(data[info.index]);
        },
        billboard: true,
        getIcon: d => d.buyerOrSeller,
        getPixelOffset: [0, 90],
        getPosition: d => [d.lon, d.lat, d.alt],
        getSize: d => 125,
        getColor: d => [0, 0, 0, secondsToAlpha(d.lastStdOutMessageTimestamp >= d.lastStdInMessageTimestamp ? d.lastStdOutMessageTimestamp : d.lastStdInMessageTimestamp)],
        iconAtlas: './images/map-icon-map.png',
        iconMapping: {
            buyer: {
                x: 0,
                y: 0,
                width: 360,
                height: 207,
                anchorY: 360,
                mask: false
            },
            seller: {
                x: 360,
                y: 0,
                width: 360,
                height: 207,
                anchorY: 360,
                mask: false
            },
        }
    });


    const textLayer = new deck.TextLayer({
        id: 'textLayer',
        billboard: true,
        data,
        pickable: false,
        getTextAnchor: 'start',
        getPosition: d => [d.lon, d.lat, d.alt],
        getText: d => "Eth addr: ..." + d.address.slice(-5) + "\n" +
            "Seen:       " + secondsSince(d.lastStdOutMessageTimestamp) + " ago\n" +
            "RFS:        " + secondsSince(d.lastStdInMessageTimestamp) + " ago"   , 
        getColor: d => [255, 255, 255, secondsToAlpha(d.lastStdOutMessageTimestamp >= d.lastStdInMessageTimestamp ? d.lastStdOutMessageTimestamp : d.lastStdInMessageTimestamp)],
        outlineColor: [255, 255, 255, 255],
        getSize: 11,
        getPixelOffset: [25, -88],
        fontWeight: 200,
        background: false,

        getBackgroundColor: [0, 0, 0],
        backgroundPadding: [2, 2],

        getTextAnchor: 'middle',
        getAlignmentBaseline: 'center'
    });

    var dataArcs = [];

    data.forEach(d => {
        const sourceCoords = peerIdToXYX[d.address];
        d.talkingToPeerIDs.forEach(target => {
            fullTarget  =  last4ToFullAddress[target];
            const targetCoords = peerIdToXYX[fullTarget]
            if (targetCoords != undefined) {
                dataArcs.push([sourceCoords, targetCoords, d.lastStdOutMessageTimestamp]);
            }
        })

    });

    

    const talkingToArcLayer = new deck.ArcLayer({
        id: 'sensorArcLayer',
      
        data: dataArcs,
        getSourcePosition: d => [d[0][0]+0.001, d[0][1], d[0][2]], 
        getTargetPosition: d => [d[1][0], d[1][1], d[1][2]],
        getSourceColor: d => [95,95,95, secondsToAlpha(d[2])  ],
        getTargetColor: d => [95,95, 95, secondsToAlpha(d[2])],
       
        getWidth: 1,
        getHeight: 0
    });


    let layersList = [
        iconLayer,
        textLayer,
        deviceTrackLayer
    ];
    if (lineson) {
        layersList.push(talkingToArcLayer);
    }

    mydeckgl.setProps({ layers: layersList });

    function secondsToAlpha(timestamp) {
        const seconds = secondsSinceRaw(timestamp);
        if (typeof seconds !== "number") {
            return undefined;
        }
        if (seconds < 0) {
            return 245;
        }
        if (seconds > 240) {
            return 1;
        }
        return (240 - seconds) * (245 / 240);
    }


    function secondsSinceRaw(timestamp) {
        let now = new Date();
        let then = new Date(timestamp);
        let diff = now.getTime() - then.getTime();
        return Math.floor(diff / 1000);;
    }

    function secondsSince(timestamp) {
        let now = new Date();
        let then = new Date(timestamp);

        let seconds = secondsSinceRaw(timestamp);
        if (seconds < 60) {
            return seconds + "s";
        }
        let minutes = Math.floor(seconds / 60);
        if (minutes < 60) {
            return minutes + "m";
        }
        let hours = Math.floor(minutes / 60);
        if (hours < 24) {
            return hours + "h";
        }
        let days = Math.floor(hours / 24);
        if (days < 30) {
            return days + "d";
        }
        let months = Math.floor(days / 30);
        if (months < 12) {
            return months + "m";
        }
        let years = Math.floor(months / 12);
        return years+ "y";
    }

    async function peerToCard(deviceInfo) {

        let lastStdOutMessageJson = {};
        try {
            lastStdOutMessageJson = JSON.parse(deviceInfo['lastStdOutMessage']);
        } catch (e) {
            console.log("Failed to parse lastStdOutMessage as JSON: " + e);
        }

        let ethAddr = deviceInfo['address'];
        document.getElementById("ethPeerAddressShort").innerHTML = ethAddr.slice(0, 4) + "..." + ethAddr.slice(-4);
        document.getElementById("ethPeerAddress").innerHTML = ethAddr;
        document.getElementById("hbarBalance").innerHTML = (deviceInfo['balance'] / 1_000_000_000_000_000_000).toFixed(2);
        document.getElementById("explorerButton").href = 'https://hashscan.io/testnet/account/' + ethAddr;
        document.getElementById("lastSeen").innerHTML = secondsSince(deviceInfo.lastStdOutMessageTimestamp);
        document.getElementById("numberOfHeartbeats").innerHTML = deviceInfo.stdOutSequenceNumber;
        document.getElementById("numberOfRequests").innerHTML = deviceInfo.stdInSequenceNumber;
        document.getElementById("connections").innerHTML = deviceInfo.talkingToPeerIDs.length;
        if (lastStdOutMessageJson == null) {
            document.getElementById("connectivityInfo").innerHTML = "false";// make it red totally unreachable
        
        } else {
            document.getElementById("connectivityInfo").innerHTML = lastStdOutMessageJson.natReachability ;
        
        }
        
        document.getElementById("stdOutTopic").innerHTML = deviceInfo.stdOutTopic;
        document.getElementById("stdOutTopicLink").href = 'https://hashscan.io/testnet/topic/' + deviceInfo.stdOutTopic;
        document.getElementById("lastHeartbeat").innerHTML = deviceInfo.lastStdOutMessageTimestamp;
        document.getElementById("sdkVersion").innerHTML = deviceInfo.sdkVersion == null ? "unknown" : deviceInfo.sdkVersion;
        

        document.getElementById("stdInTopic").innerHTML = deviceInfo.stdInTopic;
        document.getElementById("stdInTopicLink").href = 'https://hashscan.io/testnet/topic/' + deviceInfo.stdInTopic;
        
        document.getElementById("latestStdInMessages").innerHTML="";
        var ms =deviceInfo.latestStdInMessages;
        for (i in ms ) {
            var e = ms[i].map["e"]
            var ethAddrShort =   e.slice(0, 4) + "..." + e.slice(-4);
            var ss = secondsSince( new Date( ms[i].map["timestamp"]  * 1000));
            document.getElementById("latestStdInMessages").innerHTML += `<tr><td>${ethAddrShort}</td><td>0.0.${ms[i].map["a"]}</td><td>${ss}</td></tr>`;;
        }

        flipCard('back');
    }

    function getStats() {
        fetch('getStats', {method: 'GET'})
            .then(function (response){return response.json()})
            .then(function (json){
            console.log(json);
            document.getElementById("total-txs").innerHTML = json.totalNumberOfTransactions.toLocaleString();
            document.getElementById("trackedDiv").innerHTML = json.totalNumberOfConnections;
            document.getElementById("aliveDiv").innerHTML = json.totalNumberOfActiveDevices;
            });

    }


}