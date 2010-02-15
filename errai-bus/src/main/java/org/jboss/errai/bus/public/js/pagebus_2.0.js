/**
 * TIBCO PageBus(TM) version 2.0.0
 * 
 * Copyright (c) 2006-2009, TIBCO Software Inc.
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not 
 * use this file except in compliance with the License. You may obtain a copy 
 * of the License at http://www.apache.org/licenses/LICENSE-2.0 . Unless
 * required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR 
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the 
 * specific language governing permissions and limitations under the License.
 *
 *
 * Includes code from the official reference implementation of the OpenAjax
 * Hub that is provided by OpenAjax Alliance. Specification is available at:
 *
 *  http://www.openajax.org/member/wiki/OpenAjax_Hub_Specification
 *
 * Copyright 2006-2009 OpenAjax Alliance
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not 
 * use this file except in compliance with the License. You may obtain a copy 
 * of the License at http://www.apache.org/licenses/LICENSE-2.0 . Unless
 * required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR 
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the 
 * specific language governing permissions and limitations under the License.
 *
 ******************************************************************************/

// prevent re-definition of the OpenAjax object
if ( !window["OpenAjax"] ) {

OpenAjax = new function() {
    this.hub = {};
    h = this.hub;
    h.implementer = "http://openajax.org";
    h.implVersion = "2.0";
    h.specVersion = "2.0";
    h.implExtraData = {};
    var libs = {};
    h.libraries = libs;
    var ooh = "org.openajax.hub.";

    h.registerLibrary = function(prefix, nsURL, version, extra){
        libs[prefix] = {
            prefix: prefix,
            namespaceURI: nsURL,
            version: version,
            extraData: extra 
        };
        this.publish(ooh+"registerLibrary", libs[prefix]);
    }
    h.unregisterLibrary = function(prefix){
        this.publish(ooh+"unregisterLibrary", libs[prefix]);
        delete libs[prefix];
    }
}

/**
 * Error
 * 
 * Standard Error names used when the standard functions need to throw Errors.
 */
OpenAjax.hub.Error = {
    // Either a required argument is missing or an invalid argument was provided
    BadParameters: "OpenAjax.hub.Error.BadParameters",
    // The specified hub has been disconnected and cannot perform the requested
    // operation:
    Disconnected: "OpenAjax.hub.Error.Disconnected",
    // Container with specified ID already exists:
    Duplicate: "OpenAjax.hub.Error.Duplicate",
    // The specified ManagedHub has no such Container (or it has been removed)
    NoContainer: "OpenAjax.hub.Error.NoContainer",
    // The specified ManagedHub or Container has no such subscription
    NoSubscription: "OpenAjax.hub.Error.NoSubscription",
    // Permission denied by manager's security policy
    NotAllowed: "OpenAjax.hub.Error.NotAllowed",
    // Wrong communications protocol identifier provided by Container or HubClient
    WrongProtocol: "OpenAjax.hub.Error.WrongProtocol"
};

/**
 * SecurityAlert
 * 
 * Standard codes used when attempted security violations are detected. Unlike
 * Errors, these codes are not thrown as exceptions but rather passed into the 
 * SecurityAlertHandler function registered with the Hub instance.
 */
OpenAjax.hub.SecurityAlert = {
    // Container did not load (possible frame phishing attack)
    LoadTimeout: "OpenAjax.hub.SecurityAlert.LoadTimeout",
    // Hub suspects a frame phishing attack against the specified container
    FramePhish: "OpenAjax.hub.SecurityAlert.FramePhish",
    // Hub detected a message forgery that purports to come to a specifed
    // container
    ForgedMsg: "OpenAjax.hub.SecurityAlert.ForgedMsg"
};

/**
 * Debugging Help
 *
 * OpenAjax.hub.enableDebug
 *
 *      If OpenAjax.hub.enableDebug is set to true, then the "debugger" keyword
 *      will get hit whenever a user callback throws an exception, thereby
 *      bringing up the JavaScript debugger.
 */
OpenAjax.hub._debugger = function() {
    if ( OpenAjax.hub.enableDebug ) debugger; // REMOVE ON BUILD
}

////////////////////////////////////////////////////////////////////////////////

/**
 * Hub interface
 * 
 * Hub is implemented on the manager side by ManagedHub and on the client side
 * by ClientHub.
 */
//OpenAjax.hub.Hub = function() {}

/**
 * Subscribe to a topic.
 *
 * @param {String} topic
 *     A valid topic string. MAY include wildcards.
 * @param {Function} onData   
 *     Callback function that is invoked whenever an event is 
 *     published on the topic
 * @param {Object} [scope]
 *     When onData callback or onComplete callback is invoked,
 *     the JavaScript "this" keyword refers to this scope object.
 *     If no scope is provided, default is window.
 * @param {Function} [onComplete]
 *     Invoked to tell the client application whether the 
 *     subscribe operation succeeded or failed. 
 * @param {*} [subscriberData]
 *     Client application provides this data, which is handed
 *     back to the client application in the subscriberData
 *     parameter of the onData callback function.
 * 
 * @returns subscriptionID
 *     Identifier representing the subscription. This identifier is an 
 *     arbitrary ID string that is unique within this Hub instance
 * @type {String}
 * 
 * @throws {OpenAjax.hub.Error.Disconnected} if this Hub instance is not in CONNECTED state
 * @throws {OpenAjax.hub.Error.BadParameters} if the topic is invalid (e.g. contains an empty token)
 */
//OpenAjax.hub.Hub.prototype.subscribe = function( topic, onData, scope, onComplete, subscriberData ) {}

/**
 * Publish an event on a topic
 *
 * @param {String} topic
 *     A valid topic string. MUST NOT include wildcards.
 * @param {*} data
 *     Valid publishable data. To be portable across different
 *     Container implementations, this value SHOULD be serializable
 *     as JSON.
 *     
 * @throws {OpenAjax.hub.Error.Disconnected} if this Hub instance is not in CONNECTED state
 * @throws {OpenAjax.hub.Error.BadParameters} if the topic cannot be published (e.g. contains 
 *     wildcards or empty tokens) or if the data cannot be published (e.g. cannot be serialized as JSON)
 */
//OpenAjax.hub.Hub.prototype.publish = function( topic, data ) {}

/**
 * Unsubscribe from a subscription
 *
 * @param {String} subscriptionID
 *     A subscriptionID returned by Hub.subscribe()
 * @param {Function} [onComplete]
 *     Callback function invoked when unsubscribe completes
 * @param {Object} [scope]
 *     When onComplete callback function is invoked, the JavaScript "this"
 *     keyword refers to this scope object.
 *     If no scope is provided, default is window.
 *     
 * @throws {OpenAjax.hub.Error.Disconnected} if this Hub instance is not in CONNECTED state
 * @throws {OpenAjax.hub.Error.NoSubscription} if no such subscription is found
 */
//OpenAjax.hub.Hub.prototype.unsubscribe = function( subscriptionID, onComplete, scope ) {}

/**
 * Return true if this Hub instance is in the Connected state.
 * Else returns false.
 * 
 * This function can be called even if the Hub is not in a CONNECTED state.
 * 
 * @returns Boolean
 * @type {Boolean}
 */
//OpenAjax.hub.Hub.prototype.isConnected = function() {}

/**
 * Returns the scope associated with this Hub instance and which will be used
 * with callback functions.
 * 
 * This function can be called even if the Hub is not in a CONNECTED state.
 * 
 * @returns scope object
 * @type {Object}
 */
//OpenAjax.hub.Hub.prototype.getScope = function() {}

/**
 * Returns the subscriberData parameter that was provided when 
 * Hub.subscribe was called.
 *
 * @param {String} subscriberID
 *     The subscriberID of a subscription
 * 
 * @returns subscriberData
 * @type {*}
 * 
 * @throws {OpenAjax.hub.Error.Disconnected} if this Hub instance is not in CONNECTED state
 * @throws {OpenAjax.hub.Error.NoSubscription} if there is no such subscription
 */
//OpenAjax.hub.Hub.prototype.getSubscriberData = function(subscriberID) {}

/**
 * Returns the scope associated with a specified subscription.  This scope will
 * be used when invoking the 'onData' callback supplied to Hub.subscribe().
 *
 * @param {String} subscriberID
 *     The subscriberID of a subscription
 * 
 * @returns scope
 * @type {*}
 * 
 * @throws {OpenAjax.hub.Error.Disconnected} if this Hub instance is not in CONNECTED state
 * @throws {OpenAjax.hub.Error.NoSubscription} if there is no such subscription
 */
//OpenAjax.hub.Hub.prototype.getSubscriberScope = function(subscriberID) {}

/**
 * Returns the params object associated with this Hub instance.
 * Allows mix-in code to access parameters passed into constructor that created
 * this Hub instance.
 *
 * @returns params  the params object associated with this Hub instance
 * @type {Object}
 */
//OpenAjax.hub.Hub.prototype.getParameters = function() {}

////////////////////////////////////////////////////////////////////////////////

/**
 * HubClient interface 
 * 
 * Extends Hub interface.
 * 
 * A HubClient implementation is typically specific to a particular 
 * implementation of Container.
 */

/**
 * Create a new HubClient. All HubClient constructors MUST have this 
 * signature.
 * @constructor
 * 
 * @param {Object} params 
 *    Parameters used to instantiate the HubClient.
 *    Once the constructor is called, the params object belongs to the
 *    HubClient. The caller MUST not modify it.
 *    Implementations of HubClient may specify additional properties
 *    for the params object, besides those identified below. 
 * 
 * @param {Function} params.HubClient.onSecurityAlert
 *     Called when an attempted security breach is thwarted
 * @param {Object} [params.HubClient.scope]
 *     Whenever one of the HubClient's callback functions is called,
 *     references to "this" in the callback will refer to the scope object.
 *     If not provided, the default is window.
 *     
 * @throws {OpenAjax.hub.Error.BadParameters} if any of the required
 *     parameters is missing, or if a parameter value is invalid in 
 *     some way.
 */
//OpenAjax.hub.HubClient = function( params ) {}

/**
 * Requests a connection to the ManagedHub, via the Container
 * associated with this HubClient.
 * 
 * If the Container accepts the connection request, the HubClient's 
 * state is set to CONNECTED and the HubClient invokes the 
 * onComplete callback function.
 * 
 * If the Container refuses the connection request, the HubClient
 * invokes the onComplete callback function with an error code. 
 * The error code might, for example, indicate that the Container 
 * is being destroyed.
 * 
 * In most implementations, this function operates asynchronously, 
 * so the onComplete callback function is the only reliable way to
 * determine when this function completes and whether it has succeeded
 * or failed.
 * 
 * A client application may call HubClient.disconnect and then call
 * HubClient.connect.
 * 
 * @param {Function} [onComplete]
 *     Callback function to call when this operation completes.
 * @param {Object} [scope]  
 *     When the onComplete function is invoked, the JavaScript "this"
 *     keyword refers to this scope object.
 *     If no scope is provided, default is window.
 *
 * @throws {OpenAjax.hub.Error.Duplicate} if the HubClient is already connected
 */
//OpenAjax.hub.HubClient.prototype.connect = function( onComplete, scope ) {}

/**
 * Disconnect from the ManagedHub
 * 
 * Disconnect immediately:
 * 
 * 1. Sets the HubClient's state to DISCONNECTED.
 * 2. Causes the HubClient to send a Disconnect request to the 
 * 		associated Container. 
 * 3. Ensures that the client application will receive no more
 * 		onData or onComplete callbacks associated with this 
 * 		connection, except for the disconnect function's own
 * 		onComplete callback.
 * 4. Automatically destroys all of the HubClient's subscriptions.
 *
 * In most implementations, this function operates asynchronously, 
 * so the onComplete callback function is the only reliable way to
 * determine when this function completes and whether it has succeeded
 * or failed.
 * 
 * A client application is allowed to call HubClient.disconnect and 
 * then call HubClient.connect.
 * 	
 * @param {Function} [onComplete]
 *     Callback function to call when this operation completes.
 * @param {Object} [scope]  
 *     When the onComplete function is invoked, the JavaScript "this"
 *     keyword refers to the scope object.
 *     If no scope is provided, default is window.
 *
 * @throws {OpenAjax.hub.Error.Disconnected} if the HubClient is already
 *     disconnected
 */
//OpenAjax.hub.HubClient.prototype.disconnect = function( onComplete, scope ) {}

/**
 * If DISCONNECTED: Returns null
 * If CONNECTED: Returns the origin associated with the window containing the
 * Container associated with this HubClient instance. The origin has the format
 *  
 * [protocol]://[host]
 * 
 * where:
 * 
 * [protocol] is "http" or "https"
 * [host] is the hostname of the partner page.
 * 
 * @returns Partner's origin
 * @type {String}
 */
//OpenAjax.hub.HubClient.prototype.getPartnerOrigin = function() {}

/**
 * Returns the client ID of this HubClient
 *
 * @returns clientID
 * @type {String}
 */
//OpenAjax.hub.HubClient.prototype.getClientID = function() {}

////////////////////////////////////////////////////////////////////////////////

/**
 * OpenAjax.hub.ManagedHub
 *
 * Managed hub API for the manager application and for Containers. 
 * 
 * Implements OpenAjax.hub.Hub.
 */

/**
 * Create a new ManagedHub instance
 * @constructor
 *     
 * This constructor automatically sets the ManagedHub's state to
 * CONNECTED.
 * 
 * @param {Object} params
 *     Parameters used to instantiate the ManagedHub.
 *     Once the constructor is called, the params object belongs exclusively to
 *     the ManagedHub. The caller MUST not modify it.
 *     
 * The params object may contain the following properties:
 * 
 * @param {Function} params.onPublish
 *     Callback function that is invoked whenever a 
 *     data value published by a Container is about
 *     to be delivered to some (possibly the same) Container.
 *     This callback function implements a security policy;
 *     it returns true if the delivery of the data is
 *     permitted and false if permission is denied.
 * @param {Function} params.onSubscribe
 *     Called whenever a Container tries to subscribe
 *     on behalf of its client.
 *     This callback function implements a security policy;
 *     it returns true if the subscription is permitted 
 *     and false if permission is denied.
 * @param {Function} [params.onUnsubscribe]
 *     Called whenever a Container unsubscribes on behalf of its client. 
 *     Unlike the other callbacks, onUnsubscribe is intended only for 
 *     informative purposes, and is not used to implement a security
 *     policy.
 * @param {Object} [params.scope]
 *     Whenever one of the ManagedHub's callback functions is called,
 *     references to the JavaScript "this" keyword in the callback 
 *     function refer to this scope object
 *     If no scope is provided, default is window.
 * @param {Function} [params.log]  Optional logger function. Would
 *     be used to log to console.log or equivalent.
 * 
 * @throws {OpenAjax.hub.Error.BadParameters} if any of the required
 *     parameters are missing
 */
OpenAjax.hub.ManagedHub = function( params )
{
    if ( ! params || ! params.onPublish || ! params.onSubscribe )
        throw new Error( OpenAjax.hub.Error.BadParameters );
    
    this._p = params;
    this._onUnsubscribe = params.onUnsubscribe ? params.onUnsubscribe : null;
    this._scope = params.scope || window;

    if ( params.log ) {
        var scope = this._scope;
        var logfunc = params.log;
        this._log = function( msg ) {
            logfunc.call( scope, "ManagedHub: " + msg );
        };
    } else {
        this._log = function() {};
    }

    this._subscriptions = { c:{}, s:null };
    this._containers = {};

    // Sequence # used to create IDs that are unique within this hub
    this._seq = 0;

    this._active = true;
    
    this._isPublishing = false;
    this._pubQ = [];
}

/**
 * Subscribe to a topic on behalf of a Container. Called only by 
 * Container implementations, NOT by manager applications.
 * 
 * This function:
 * 1. Checks with the ManagedHub's onSubscribe security policy
 *    to determine whether this Container is allowed to subscribe 
 *    to this topic.
 * 2. If the subscribe operation is permitted, subscribes to the
 *    topic and returns the ManagedHub's subscription ID for this
 *    subscription. 
 * 3. If the subscribe operation is not permitted, throws
 *    OpenAjax.hub.Error.NotAllowed.
 * 
 * When data is published on the topic, the ManagedHub's 
 * onPublish security policy will be invoked to ensure that
 * this Container is permitted to receive the published data.
 * If the Container is allowed to receive the data, then the
 * Container's sendToClient function will be invoked.
 * 
 * When a Container needs to create a subscription on behalf of
 * its client, the Container MUST use this function to create
 * the subscription.
 * 
 * @param {OpenAjax.hub.Container} container  
 *     A Container
 * @param {String} topic 
 *     A valid topic
 * @param {String} containerSubID  
 *     Arbitrary string ID that the Container uses to 
 *     represent the subscription. Must be unique within the 
 *     context of the Container
 *
 * @returns managerSubID  
 *     Arbitrary string ID that this ManagedHub uses to 
 *     represent the subscription. Will be unique within the 
 *     context of this ManagedHub
 * @type {String}
 * 
 * @throws {OpenAjax.hub.Error.Disconnected} if this.isConnected() returns false
 * @throws {OpenAjax.hub.Error.NotAllowed} if subscription request is denied by the onSubscribe security policy
 * @throws {OpenAjax.hub.Error.BadParameters} if one of the parameters, e.g. the topic, is invalid
 */
OpenAjax.hub.ManagedHub.prototype.subscribeForClient = function( container, topic, containerSubID )
{
    this._assertConn();
    // check subscribe permission
    if ( this._invokeOnSubscribe( topic, container ) ) {
        // return ManagedHub's subscriptionID for this subscription
        return this._subscribe( topic, this._sendToClient, this, { c: container, sid: containerSubID } );
    }
    throw new Error(OpenAjax.hub.Error.NotAllowed);
}

/**
 * Unsubscribe from a subscription on behalf of a Container. Called only by 
 * Container implementations, NOT by manager application code.
 * 
 * This function:
 * 1. Destroys the specified subscription
 * 2. Calls the ManagedHub's onUnsubscribe callback function
 * 
 * This function can be called even if the ManagedHub is not in a CONNECTED state.
 * 
 * @param {OpenAjax.hub.Container} container  
 *    container instance that is unsubscribing
 * @param {String} managerSubID  
 *    opaque ID of a subscription, returned by previous call to subscribeForClient()
 * 
 * @throws {OpenAjax.hub.Error.NoSubscription} if subscriptionID does not refer to a valid subscription
 */
OpenAjax.hub.ManagedHub.prototype.unsubscribeForClient = function( container, managerSubID )
{
    this._unsubscribe( managerSubID );
    this._invokeOnUnsubscribe( container, managerSubID );
}
  
/**
 * Publish data on a topic on behalf of a Container. Called only by 
 * Container implementations, NOT by manager application code.
 *
 * @param {OpenAjax.hub.Container} container
 *      Container on whose behalf data should be published
 * @param {String} topic
 *      Valid topic string. Must NOT contain wildcards.
 * @param {*} data
 *      Valid publishable data. To be portable across different
 *      Container implementations, this value SHOULD be serializable
 *      as JSON.
 * 
 * @throws {OpenAjax.hub.Error.Disconnected} if this.isConnected() returns false
 * @throws {OpenAjax.hub.Error.BadParameters} if one of the parameters, e.g. the topic, is invalid
 */
OpenAjax.hub.ManagedHub.prototype.publishForClient = function( container, topic, data )
{
    this._assertConn();
    this._publish( topic, data, container );
}

/**
 * Destroy this ManagedHub
 * 
 * 1. Sets state to DISCONNECTED. All subsequent attempts to add containers,
 *  publish or subscribe will throw the Disconnected error. We will
 *  continue to allow "cleanup" operations such as removeContainer
 *  and unsubscribe, as well as read-only operations such as 
 *  isConnected
 * 2. Remove all Containers associated with this ManagedHub
 */
OpenAjax.hub.ManagedHub.prototype.disconnect = function()
{
    this._active = false;
    for (var c in this._containers) {
        this.removeContainer( this._containers[c] );
    }
}

/**
 * Get a container belonging to this ManagedHub by its clientID, or null
 * if this ManagedHub has no such container
 * 
 * This function can be called even if the ManagedHub is not in a CONNECTED state.
 * 
 * @param {String} containerId
 *      Arbitrary string ID associated with the container
 *
 * @returns container associated with given ID
 * @type {OpenAjax.hub.Container}
 */
OpenAjax.hub.ManagedHub.prototype.getContainer = function( containerId ) 
{
    var container = this._containers[containerId];
    return container ? container : null;
}

/**
 * Returns an array listing all containers belonging to this ManagedHub.
 * The order of the Containers in this array is arbitrary.
 * 
 * This function can be called even if the ManagedHub is not in a CONNECTED state.
 * 
 * @returns container array
 * @type {OpenAjax.hub.Container[]}
 */
OpenAjax.hub.ManagedHub.prototype.listContainers = function() 
{
    var res = [];
    for (var c in this._containers) { 
        res.push(this._containers[c]);
    }
    return res;
}

/**
 * Add a container to this ManagedHub.
 *
 * This function should only be called by a Container constructor.
 * 
 * @param {OpenAjax.hub.Container} container
 *      A Container to be added to this ManagedHub
 * 
 * @throws {OpenAjax.hub.Error.Duplicate} if there is already a Container
 *      in this ManagedHub whose clientId is the same as that of container
 * @throws {OpenAjax.hub.Error.Disconnected} if this.isConnected() returns false
 */
OpenAjax.hub.ManagedHub.prototype.addContainer = function( container ) 
{ 
    this._assertConn();
    var containerId = container.getClientID();
    if ( this._containers[containerId] ) {
        throw new Error(OpenAjax.hub.Error.Duplicate);
    }
    this._containers[containerId] = container;
}

/**
 * Remove a container from this ManagedHub immediately
 * 
 * This function can be called even if the ManagedHub is not in a CONNECTED state.
 * 
 * @param {OpenAjax.hub.Container} container  
 *      A Container to be removed from this ManagedHub
 *  
 * @throws {OpenAjax.hub.Error.NoContainer}  if no such container is found
 */
OpenAjax.hub.ManagedHub.prototype.removeContainer = function( container )
{
    var containerId = container.getClientID();
    if ( ! this._containers[ containerId ] ) {
        throw new Error(OpenAjax.hub.Error.NoContainer);
    }
    container.remove();
    delete this._containers[ containerId ];
}

    /*** OpenAjax.hub.Hub interface implementation ***/

/**
 * Subscribe to a topic.
 * 
 * This implementation of Hub.subscribe is synchronous. When subscribe 
 * is called:
 * 
 * 1. The ManagedHub's onSubscribe callback is invoked. The 
 * 		container parameter is null, because the manager application, 
 * 		rather than a container, is subscribing.
 * 2. If onSubscribe returns true, then the subscription is created.
 * 3. The onComplete callback is invoked.
 * 4. Then this function returns.
 * 
 * @param {String} topic
 *     A valid topic string. MAY include wildcards.
 * @param {Function} onData   
 *     Callback function that is invoked whenever an event is 
 *     published on the topic
 * @param {Object} [scope]
 *     When onData callback or onComplete callback is invoked,
 *     the JavaScript "this" keyword refers to this scope object.
 *     If no scope is provided, default is window.
 * @param {Function} [onComplete]
 *     Invoked to tell the client application whether the 
 *     subscribe operation succeeded or failed. 
 * @param {*} [subscriberData]
 *     Client application provides this data, which is handed
 *     back to the client application in the subscriberData
 *     parameter of the onData and onComplete callback functions.
 * 
 * @returns subscriptionID
 *     Identifier representing the subscription. This identifier is an 
 *     arbitrary ID string that is unique within this Hub instance
 * @type {String}
 * 
 * @throws {OpenAjax.hub.Error.Disconnected} if this Hub instance is not in CONNECTED state
 * @throws {OpenAjax.hub.Error.BadParameters} if the topic is invalid (e.g. contains an empty token)
 */
OpenAjax.hub.ManagedHub.prototype.subscribe = function( topic, onData, scope, onComplete, subscriberData ) 
{
    this._assertConn();
    this._assertSubTopic(topic);
    if ( ! onData ) {
        throw new Error( OpenAjax.hub.Error.BadParameters );
    }
    
    // check subscribe permission
    if ( ! this._invokeOnSubscribe( topic, null ) ) {
        this._invokeOnComplete( onComplete, scope, null, false, OpenAjax.hub.Error.NotAllowed );
        return;
    }
    
    // on publish event, check publish permissions
    scope = scope || window;
    var that = this;
    function publishCB( topic, data, sd, pcont ) {
        if ( that._invokeOnPublish( topic, data, pcont, null ) ) {
            try {
            	onData.call( scope, topic, data, subscriberData );
            } catch( e ) {
                OpenAjax.hub._debugger();
                that._log( "caught error from onData callback to Hub.subscribe(): " + e.message );
            }
        }
    }
    
    var subID = this._subscribe( topic, publishCB, scope, subscriberData );
    
    this._invokeOnComplete( onComplete, scope, subID, true );
    return subID;
}

/**
 * Publish an event on a topic
 *
 * This implementation of Hub.publish is synchronous. When publish 
 * is called:
 * 
 * 1. The target subscriptions are identified.
 * 2. For each target subscription, the ManagedHub's onPublish
 * 		callback is invoked. Data is only delivered to a target
 * 		subscription if the onPublish callback returns true.
 * 		The pcont parameter of the onPublish callback is null.
 *      This is because the ManagedHub, rather than a container,
 *      is publishing the data.
 * 
 * @param {String} topic
 *     A valid topic string. MUST NOT include wildcards.
 * @param {*} data
 *     Valid publishable data. To be portable across different
 *     Container implementations, this value SHOULD be serializable
 *     as JSON.
 *     
 * @throws {OpenAjax.hub.Error.Disconnected} if this Hub instance is not in CONNECTED state
 * @throws {OpenAjax.hub.Error.BadParameters} if the topic cannot be published (e.g. contains 
 *     wildcards or empty tokens) or if the data cannot be published (e.g. cannot be serialized as JSON)
 */
OpenAjax.hub.ManagedHub.prototype.publish = function( topic, data ) 
{
    this._assertConn();
    this._assertPubTopic(topic);
    this._publish( topic, data, null );
}

/**
 * Unsubscribe from a subscription
 * 
 * This implementation of Hub.unsubscribe is synchronous. When unsubscribe 
 * is called:
 * 
 * 1. The subscription is destroyed.
 * 2. The ManagedHub's onUnsubscribe callback is invoked, if there is one.
 * 3. The onComplete callback is invoked.
 * 4. Then this function returns.
 * 
 * @param {String} subscriptionID
 *     A subscriptionID returned by Hub.subscribe()
 * @param {Function} [onComplete]
 *     Callback function invoked when unsubscribe completes
 * @param {Object} [scope]
 *     When onComplete callback function is invoked, the JavaScript "this"
 *     keyword refers to this scope object.
 *     If no scope is provided, default is window.
 *     
 * @throws {OpenAjax.hub.Error.Disconnected} if this Hub instance is not in CONNECTED state
 * @throws {OpenAjax.hub.Error.NoSubscription} if no such subscription is found
 */
OpenAjax.hub.ManagedHub.prototype.unsubscribe = function( subscriptionID, onComplete, scope )
{
    this._assertConn();
    if ( typeof subscriptionID === "undefined" || subscriptionID == null ) {
        throw new Error( OpenAjax.hub.Error.BadParameters );
    }
    this._unsubscribe( subscriptionID );
    this._invokeOnUnsubscribe( null, subscriptionID );
    this._invokeOnComplete( onComplete, scope, subscriptionID, true );
}

/**
 * Returns true if disconnect() has NOT been called on this ManagedHub, 
 * else returns false
 * 
 * @returns Boolean
 * @type {Boolean}
 */
OpenAjax.hub.ManagedHub.prototype.isConnected = function()
{
    return this._active;
}

/**
* Returns the scope associated with this Hub instance and which will be used
* with callback functions.
* 
* This function can be called even if the Hub is not in a CONNECTED state.
* 
* @returns scope object
* @type {Object}
 */
OpenAjax.hub.ManagedHub.prototype.getScope = function()
{
    return this._scope;
}

/**
 * Returns the subscriberData parameter that was provided when 
 * Hub.subscribe was called.
 *
 * @param subscriberID
 *     The subscriberID of a subscription
 * 
 * @returns subscriberData
 * @type {*}
 * 
 * @throws {OpenAjax.hub.Error.Disconnected} if this Hub instance is not in CONNECTED state
 * @throws {OpenAjax.hub.Error.NoSubscription} if there is no such subscription
 */
OpenAjax.hub.ManagedHub.prototype.getSubscriberData = function( subscriberID )
{
    this._assertConn();
    var path = subscriberID.split(".");
    var sid = path.pop();
    var sub = this._getSubscriptionObject( this._subscriptions, path, 0, sid );
    if ( sub ) 
        return sub.data;
    throw new Error( OpenAjax.hub.Error.NoSubscription );
}

/**
 * Returns the scope associated with a specified subscription.  This scope will
 * be used when invoking the 'onData' callback supplied to Hub.subscribe().
 *
 * @param subscriberID
 *     The subscriberID of a subscription
 * 
 * @returns scope
 * @type {*}
 * 
 * @throws {OpenAjax.hub.Error.Disconnected} if this Hub instance is not in CONNECTED state
 * @throws {OpenAjax.hub.Error.NoSubscription} if there is no such subscription
 */
OpenAjax.hub.ManagedHub.prototype.getSubscriberScope = function( subscriberID )
{
    this._assertConn();
    var path = subscriberID.split(".");
    var sid = path.pop();
    var sub = this._getSubscriptionObject( this._subscriptions, path, 0, sid );
    if ( sub ) 
        return sub.scope;
    throw new Error( OpenAjax.hub.Error.NoSubscription );
}

/**
 * Returns the params object associated with this Hub instance.
 * Allows mix-in code to access parameters passed into constructor that created
 * this Hub instance.
 *
 * @returns params  the params object associated with this Hub instance
 * @type {Object}
 */
OpenAjax.hub.ManagedHub.prototype.getParameters = function()
{
    return this._p;
}


/* PRIVATE FUNCTIONS */

/**
 * Send a message to a container's client. 
 * This is an OAH subscriber's data callback. It is private to ManagedHub
 * and serves as an adapter between the OAH 1.0 API and Container.sendToClient.
 * 
 * @param {String} topic Topic on which data was published
 * @param {Object} data  Data to be delivered to the client
 * @param {Object} sd    Object containing properties 
 *     c: container to which data must be sent
 *     sid: subscription ID within that container
 * @param {Object} pcont  Publishing container, or null if this data was
 *      published by the manager
 */
OpenAjax.hub.ManagedHub.prototype._sendToClient = function(topic, data, sd, pcont) 
{
    if (!this.isConnected()) {
        return;
    }
    if ( this._invokeOnPublish( topic, data, pcont, sd.c ) ) {
        sd.c.sendToClient( topic, data, sd.sid );
    }
}

OpenAjax.hub.ManagedHub.prototype._assertConn = function() 
{
    if (!this.isConnected()) {
        throw new Error(OpenAjax.hub.Error.Disconnected);
    }
}

OpenAjax.hub.ManagedHub.prototype._assertPubTopic = function(topic) 
{
    if ((topic == null) || (topic == "") || (topic.indexOf("*") != -1) ||
        (topic.indexOf("..") != -1) ||  (topic.charAt(0) == ".") ||
        (topic.charAt(topic.length-1) == "."))
    {
        throw new Error(OpenAjax.hub.Error.BadParameters);
    }
}

OpenAjax.hub.ManagedHub.prototype._assertSubTopic = function(topic) 
{
    if ( ! topic ) {
        throw new Error(OpenAjax.hub.Error.BadParameters);
    }
    var path = topic.split(".");
    var len = path.length;
    for (var i = 0; i < len; i++) {
        var p = path[i];
        if ((p == "") ||
           ((p.indexOf("*") != -1) && (p != "*") && (p != "**"))) {
            throw new Error(OpenAjax.hub.Error.BadParameters);
        }
        if ((p == "**") && (i < len - 1)) {
            throw new Error(OpenAjax.hub.Error.BadParameters);
        }
    }
}

OpenAjax.hub.ManagedHub.prototype._invokeOnComplete = function( func, scope, item, success, errorCode )
{
    if ( func ) { // onComplete is optional
        try {
            scope = scope || window;
            func.call( scope, item, success, errorCode );
        } catch( e ) {
            OpenAjax.hub._debugger();
            this._log( "caught error from onComplete callback: " + e.message );
        }
    }
}

OpenAjax.hub.ManagedHub.prototype._invokeOnPublish = function( topic, data, pcont, scont )
{
    try {
        return this._p.onPublish.call( this._scope, topic, data, pcont, scont );
    } catch( e ) {
        OpenAjax.hub._debugger();
        this._log( "caught error from onPublish callback to constructor: " + e.message );
    }
    return false;
}

OpenAjax.hub.ManagedHub.prototype._invokeOnSubscribe = function( topic, container )
{
    try {
        return this._p.onSubscribe.call( this._scope, topic, container );
    } catch( e ) {
        OpenAjax.hub._debugger();
        this._log( "caught error from onSubscribe callback to constructor: " + e.message );
    }
    return false;
}

OpenAjax.hub.ManagedHub.prototype._invokeOnUnsubscribe = function( container, managerSubID )
{
    if ( this._onUnsubscribe ) {
        var topic = managerSubID.slice( 0, managerSubID.lastIndexOf(".") );
        try {
            this._onUnsubscribe.call( this._scope, topic, container );
        } catch( e ) {
            OpenAjax.hub._debugger();
            this._log( "caught error from onUnsubscribe callback to constructor: " + e.message );
        }
    }
}

OpenAjax.hub.ManagedHub.prototype._subscribe = function( topic, onData, scope, subscriberData ) 
{
    var handle = topic + "." + this._seq;
    var sub = { scope: scope, cb: onData, data: subscriberData, sid: this._seq++ };
    var path = topic.split(".");
    this._recursiveSubscribe( this._subscriptions, path, 0, sub );
    return handle;
}

OpenAjax.hub.ManagedHub.prototype._recursiveSubscribe = function(tree, path, index, sub) 
{
    var token = path[index];
    if (index == path.length) {
        sub.next = tree.s;
        tree.s = sub;
    } else { 
        if (typeof tree.c == "undefined") {
             tree.c = {};
         }
        if (typeof tree.c[token] == "undefined") {
            tree.c[token] = { c: {}, s: null }; 
            this._recursiveSubscribe(tree.c[token], path, index + 1, sub);
        } else {
            this._recursiveSubscribe( tree.c[token], path, index + 1, sub);
        }
    }
}

OpenAjax.hub.ManagedHub.prototype._publish = function( topic, data, pcont )
{
    // if we are currently handling a publish event, then queue this request
    // and handle later, one by one
    if ( this._isPublishing ) {
        this._pubQ.push( { t: topic, d: data, p: pcont } );
        return;
    }
    
    this._safePublish( topic, data, pcont );
    
    while ( this._pubQ.length > 0 ) {
        var pub = this._pubQ.shift();
        this._safePublish( pub.t, pub.d, pub.p );
    }
}

OpenAjax.hub.ManagedHub.prototype._safePublish = function( topic, data, pcont )
{
    this._isPublishing = true;
    var path = topic.split(".");
    this._recursivePublish( this._subscriptions, path, 0, topic, data, pcont );
    this._isPublishing = false;
}

OpenAjax.hub.ManagedHub.prototype._recursivePublish = function(tree, path, index, name, msg, pcont) 
{
    if (typeof tree != "undefined") {
        var node;
        if (index == path.length) {
            node = tree;
        } else {
            this._recursivePublish(tree.c[path[index]], path, index + 1, name, msg, pcont);
            this._recursivePublish(tree.c["*"], path, index + 1, name, msg, pcont);
            node = tree.c["**"];
        }
        if (typeof node != "undefined") {
            var sub = node.s;
            while ( sub ) {
                var sc = sub.scope;
                var cb = sub.cb;
                var d = sub.data;
                var sid = sub.sid;
                if (typeof cb == "string") {
                    // get a function object
                    cb = sc[cb];
                }
                cb.call(sc, name, msg, d, pcont);
                sub = sub.next;
            }
        }
    }
}

OpenAjax.hub.ManagedHub.prototype._unsubscribe = function( subscriptionID )
{
    var path = subscriptionID.split(".");
    var sid = path.pop();
    if ( ! this._recursiveUnsubscribe( this._subscriptions, path, 0, sid ) ) {
        throw new Error( OpenAjax.hub.Error.NoSubscription );
    }
}

/**
 * @returns 'true' if properly unsubscribed; 'false' otherwise
 */
OpenAjax.hub.ManagedHub.prototype._recursiveUnsubscribe = function(tree, path, index, sid) 
{
    if ( typeof tree == "undefined" ) {
        return false;
    }
    
    if (index < path.length) {
        var childNode = tree.c[path[index]];
        if ( ! childNode ) {
            return false;
        }
        this._recursiveUnsubscribe(childNode, path, index + 1, sid);
        if (childNode.s == null) {
            for (var x in childNode.c) {
                return true;
            }
            delete tree.c[path[index]];    
        }
    } else {
        var sub = tree.s;
        var sub_prev = null;
        var found = false;
        while ( sub ) {
            if ( sid == sub.sid ) {
                found = true;
                if ( sub == tree.s ) {
                    tree.s = sub.next;
                } else {
                    sub_prev.next = sub.next;
                }
                break;
            }
            sub_prev = sub;
            sub = sub.next;
        }
        if ( ! found ) {
            return false;
        }
    }
    
    return true;
}

OpenAjax.hub.ManagedHub.prototype._getSubscriptionObject = function( tree, path, index, sid )
{
    if (typeof tree != "undefined") {
        if (index < path.length) {
            var childNode = tree.c[path[index]];
            return this._getSubscriptionObject(childNode, path, index + 1, sid);
        }

        var sub = tree.s;
        while ( sub ) {
            if ( sid == sub.sid ) {
                return sub;
            }
            sub = sub.next;
        }
    }
    return null;
}


////////////////////////////////////////////////////////////////////////////////

/**
 * Container
 * @constructor
 * 
 * Container represents an instance of a manager-side object that contains and
 * communicates with a single client of the hub. The container might be an inline
 * container, an iframe FIM container, or an iframe PostMessage container, or
 * it might be an instance of some other implementation.
 *
 * @param {OpenAjax.hub.ManagedHub} hub
 *    Managed Hub instance
 * @param {String} clientID
 *    A string ID that identifies a particular client of a Managed Hub. Unique
 *    within the context of the ManagedHub.
 * @param {Object} params  
 *    Parameters used to instantiate the Container.
 *    Once the constructor is called, the params object belongs exclusively to
 *    the Container. The caller MUST not modify it.
 *    Implementations of Container may specify additional properties
 *    for the params object, besides those identified below.
 *    The following params properties MUST be supported by all Container 
 *    implementations:
 * @param {Function} params.Container.onSecurityAlert
 *    Called when an attempted security breach is thwarted.  Function is defined
 *    as follows:  function(container, securityAlert)
 * @param {Function} [params.Container.onConnect]
 *    Called when the client connects to the Managed Hub.  Function is defined
 *    as follows:  function(container)
 * @param {Function} [params.Container.onDisconnect]
 *    Called when the client disconnects from the Managed Hub.  Function is
 *    defined as follows:  function(container)
 * @param {Object} [params.Container.scope]
 *    Whenever one of the Container's callback functions is called, references
 *    to "this" in the callback will refer to the scope object. If no scope is
 *    provided, default is window.
 * @param {Function} [params.Container.log]
 *    Optional logger function. Would be used to log to console.log or
 *    equivalent. 
 *
 * @throws {OpenAjax.hub.Error.BadParameters}   if required params are not
 *   present or null
 * @throws {OpenAjax.hub.Error.Duplicate}   if a Container with this clientID
 *   already exists in the given Managed Hub
 * @throws {OpenAjax.hub.Error.Disconnected}   if ManagedHub is not connected
 */
//OpenAjax.hub.Container = function( hub, clientID, params ) {}

/**
 * Send a message to the client inside this container. This function MUST only
 * be called by ManagedHub. 
 * 
 * @param {String} topic
 *    The topic name for the published message
 * @param {*} data
 *    The payload. Can be any JSON-serializable value.
 * @param {String} containerSubscriptionId
 *    Container's ID for a subscription, from previous call to
 *    subscribeForClient()
 */
//OpenAjax.hub.Container.prototype.sendToClient = function( topic, data, containerSubscriptionId ) {}

/**
 * Shut down a container. remove does all of the following:
 * - disconnects container from HubClient
 * - unsubscribes from all of its existing subscriptions in the ManagedHub
 * 
 * This function is only called by ManagedHub.removeContainer
 * Calling this function does NOT cause the container's onDisconnect callback to
 * be invoked.
 */
//OpenAjax.hub.Container.prototype.remove = function() {}

/**
 * Returns true if the given client is connected to the managed hub.
 * Else returns false.
 *
 * @returns true if the client is connected to the managed hub
 * @type boolean
 */
//OpenAjax.hub.Container.prototype.isConnected = function() {}

/**
 * Returns the clientID passed in when this Container was instantiated.
 *
 * @returns The clientID
 * @type {String}  
 */
//OpenAjax.hub.Container.prototype.getClientID = function() {}

/**
 * If DISCONNECTED:
 * Returns null
 * If CONNECTED:
 * Returns the origin associated with the window containing the HubClient
 * associated with this Container instance. The origin has the format
 *  
 * [protocol]://[host]
 * 
 * where:
 * 
 * [protocol] is "http" or "https"
 * [host] is the hostname of the partner page.
 * 
 * @returns Partner's origin
 * @type {String}
 */
//OpenAjax.hub.Container.prototype.getPartnerOrigin = function() {}

/**
 * Returns the params object associated with this Container instance.
 * Allows mix-in code to access parameters passed into constructor that created
 * this Container instance.
 *
 * @returns params
 *    The params object associated with this Container instance
 * @type {Object}
 */
//OpenAjax.hub.Container.prototype.getParameters = function() {}

/**
 * Returns the ManagedHub to which this Container belongs.
 *
 * @returns ManagedHub
 *         The ManagedHub object associated with this Container instance
 * @type {OpenAjax.hub.ManagedHub}
 */
//OpenAjax.hub.Container.prototype.getHub = function() {}

////////////////////////////////////////////////////////////////////////////////

/*
 * Unmanaged Hub
 */

/**
 * OpenAjax.hub._hub is the default ManagedHub instance that we use to 
 * provide OAH 1.0 behavior. 
 */
OpenAjax.hub._hub = new OpenAjax.hub.ManagedHub({ 
    onSubscribe: function(topic, ctnr) { return true; },
    onPublish: function(topic, data, pcont, scont) { return true; }
});

/**
 * Subscribe to a topic.
 *
 * @param {String} topic
 *     A valid topic string. MAY include wildcards.
 * @param {Function|String} onData
 *     Callback function that is invoked whenever an event is published on the
 *     topic.  If 'onData' is a string, then it represents the name of a
 *     function on the 'scope' object.
 * @param {Object} [scope]
 *     When onData callback is invoked,
 *     the JavaScript "this" keyword refers to this scope object.
 *     If no scope is provided, default is window.
 * @param {*} [subscriberData]
 *     Client application provides this data, which is handed
 *     back to the client application in the subscriberData
 *     parameter of the onData callback function.
 * 
 * @returns {String} Identifier representing the subscription.
 * 
 * @throws {OpenAjax.hub.Error.BadParameters} if the topic is invalid
 *     (e.g.contains an empty token)
 */
OpenAjax.hub.subscribe = function(topic, onData, scope, subscriberData) 
{
    // resolve the 'onData' function if it is a string
    if ( typeof onData === "string" ) {
        scope = scope || window;
        onData = scope[ onData ] || null;
    }
    
    return OpenAjax.hub._hub.subscribe( topic, onData, scope, null, subscriberData );
}

/**
 * Unsubscribe from a subscription.
 *
 * @param {String} subscriptionID
 *     Subscription identifier returned by subscribe()
 *     
 * @throws {OpenAjax.hub.Error.NoSubscription} if no such subscription is found
 */
OpenAjax.hub.unsubscribe = function(subscriptionID) 
{
    return OpenAjax.hub._hub.unsubscribe( subscriptionID );
}

/**
 * Publish an event on a topic.
 *
 * @param {String} topic
 *     A valid topic string. MUST NOT include wildcards.
 * @param {*} data
 *     Valid publishable data.
 *     
 * @throws {OpenAjax.hub.Error.BadParameters} if the topic cannot be published
 *     (e.g. contains wildcards or empty tokens)
 */
OpenAjax.hub.publish = function(topic, data) 
{
    OpenAjax.hub._hub.publish(topic, data);
}

////////////////////////////////////////////////////////////////////////////////

// Register the OpenAjax Hub itself as a library.
OpenAjax.hub.registerLibrary("OpenAjax", "http://openajax.org/hub", "2.0", {});

} // !window["OpenAjax"]

////////////////////////////////////////////////////////////////////////////////
        
/*

        Copyright 2006-2009 OpenAjax Alliance

        Licensed under the Apache License, Version 2.0 (the "License"); 
        you may not use this file except in compliance with the License. 
        You may obtain a copy of the License at
        
                http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software 
        distributed under the License is distributed on an "AS IS" BASIS, 
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
        See the License for the specific language governing permissions and 
        limitations under the License.
*/

/**
 * Create a new Inline Container.
 * @constructor
 * @extends OpenAjax.hub.Container
 *
 * InlineContainer implements the Container interface to provide a container
 * that places components within the same browser frame as the main mashup
 * application. As such, this container does not isolate client components into
 * secure sandboxes.
 * 
 * @param {OpenAjax.hub.ManagedHub} hub
 *    Managed Hub instance to which this Container belongs
 * @param {String} clientID
 *    A string ID that identifies a particular client of a Managed Hub. Unique
 *    within the context of the ManagedHub.
 * @param {Object} params  
 *    Parameters used to instantiate the InlineContainer.
 *    Once the constructor is called, the params object belongs exclusively to
 *    the InlineContainer. The caller MUST not modify it.
 *    The following are the pre-defined properties on params:
 * @param {Function} params.Container.onSecurityAlert
 *    Called when an attempted security breach is thwarted.  Function is defined
 *    as follows:  function(container, securityAlert)
 * @param {Function} [params.Container.onConnect]
 *    Called when the client connects to the Managed Hub.  Function is defined
 *    as follows:  function(container)
 * @param {Function} [params.Container.onDisconnect]
 *    Called when the client disconnects from the Managed Hub.  Function is
 *    defined as follows:  function(container)
 * @param {Object} [params.Container.scope]
 *    Whenever one of the Container's callback functions is called, references
 *    to "this" in the callback will refer to the scope object. If no scope is
 *    provided, default is window.
 * @param {Function} [params.Container.log]
 *    Optional logger function. Would be used to log to console.log or
 *    equivalent. 
 *
 * @throws {OpenAjax.hub.Error.BadParameters}   if required params are not
 *    present or null
 * @throws {OpenAjax.hub.Error.Duplicate}   if a Container with this clientID
 *    already exists in the given Managed Hub
 * @throws {OpenAjax.hub.Error.Disconnected}   if ManagedHub is not connected
 */
OpenAjax.hub.InlineContainer = function( hub, clientID, params )
{
    if ( ! hub || ! clientID || ! params ||
            ! params.Container || ! params.Container.onSecurityAlert ) {
        throw new Error(OpenAjax.hub.Error.BadParameters);
    }
    
    this._params = params;
    this._hub = hub;
    this._id = clientID;
    this._onSecurityAlert = params.Container.onSecurityAlert;
    this._onConnect = params.Container.onConnect ? params.Container.onConnect : null;
    this._onDisconnect = params.Container.onDisconnect ? params.Container.onDisconnect : null;
    this._scope = params.Container.scope || window;
    
    if ( params.Container.log ) {
        var scope = this._scope;
        var logfunc = params.Container.log;
        this._log = function( msg ) {
            logfunc.call( scope, "InlineContainer::" + clientID + ": " + msg );
        };
        this._doLog = true; // HW Optimization
    } else {
        this._log = function() {};
    }
    
    this._connected = false;
    this._subs = [];
    this._subIndex = 1; // HW FIX

    hub.addContainer( this );
}

    /*** OpenAjax.hub.Container interface implementation ***/

OpenAjax.hub.InlineContainer.prototype.getHub = function() {
	return this._hub;
};

OpenAjax.hub.InlineContainer.prototype.sendToClient = function( topic, data, subscriptionID )
{
    if ( this.isConnected() ) {
        var sub = this._subs[ subscriptionID ];
        try {
            sub.cb.call( sub.sc, topic, data, sub.d );
        } catch( e ) {
            OpenAjax.hub._debugger();
            this._client._log( "caught error from onData callback to HubClient.subscribe(): " + e.message );
        }
    }
}

OpenAjax.hub.InlineContainer.prototype.remove = function()
{
    if ( this.isConnected() ) {
        this._disconnect();
    }
}

OpenAjax.hub.InlineContainer.prototype.isConnected = function()
{
    return this._connected;
}

OpenAjax.hub.InlineContainer.prototype.getClientID = function()
{
    return this._id;
}

OpenAjax.hub.InlineContainer.prototype.getPartnerOrigin = function()
{
    if ( this._connected ) {
    	// HW Optimization
    	if(!this._cacheOrig)
            this._cacheOrig = window.location.protocol + "//" + window.location.hostname;
        return this._cacheOrig;
    }
    return null;
}

OpenAjax.hub.InlineContainer.prototype.getParameters = function()
{
    return this._params;
}

    /*** OpenAjax.hub.HubClient interface implementation ***/

OpenAjax.hub.InlineContainer.prototype.connect = function( client, onComplete, scope )
{
    if ( this._connected ) {
        throw new Error( OpenAjax.hub.Error.Duplicate );
    }
    
    this._connected = true;
    this._client = client;
    
    if ( this._onConnect ) {
        try {
            this._onConnect.call( this._scope, this );
        } catch( e ) {
            OpenAjax.hub._debugger();
            this._log( "caught error from onConnect callback to constructor: " + e.message );
        }
    }
    
    this._invokeOnComplete( onComplete, scope, client, true );
}

OpenAjax.hub.InlineContainer.prototype.disconnect = function( client, onComplete, scope )
{
    if ( !this._connected ) {
        throw new Error( OpenAjax.hub.Error.Disconnected );
    }
    
    this._disconnect();

    if ( this._onDisconnect ) {
        try {
            this._onDisconnect.call( this._scope, this );
        } catch( e ) {
            OpenAjax.hub._debugger();
            this._log( "caught error from onDisconnect callback to constructor: " + e.message );
        }
    }
    
    this._invokeOnComplete( onComplete, scope, client, true );
}

    /*** OpenAjax.hub.Hub interface implementation ***/

OpenAjax.hub.InlineContainer.prototype.subscribe = function( topic, onData, scope, onComplete, subscriberData )
{
    this._assertConn();
    this._assertSubTopic( topic );
    if ( ! onData ) {
        throw new Error( OpenAjax.hub.Error.BadParameters );
    }
    
    var subID = "" + this._subIndex++;
    var success = false;
    var msg = null;
    try {
        var handle = this._hub.subscribeForClient( this, topic, subID );
        success = true;
    } catch( e ) {
        // failure
        subID = null;
        msg = e.message;
    }
    
    scope = scope || window;
    if ( success ) {
        this._subs[ subID ] = { h: handle, cb: onData, sc: scope, d: subscriberData };
    }
    
    this._invokeOnComplete( onComplete, scope, subID, success, msg );
    return subID;
}

OpenAjax.hub.InlineContainer.prototype.publish = function( topic, data )
{
    this._assertConn();
    this._assertPubTopic( topic );
    this._hub.publishForClient( this, topic, data );
}

OpenAjax.hub.InlineContainer.prototype.unsubscribe = function( subscriptionID, onComplete, scope )
{
    this._assertConn();
    if ( typeof subscriptionID === "undefined" || subscriptionID == null ) {
        throw new Error( OpenAjax.hub.Error.BadParameters );
    }
    var sub = this._subs[ subscriptionID ];
    if ( ! sub ) 
        throw new Error( OpenAjax.hub.Error.NoSubscription );    
    this._hub.unsubscribeForClient( this, sub.h );
    delete this._subs[ subscriptionID ];
    
    this._invokeOnComplete( onComplete, scope, subscriptionID, true );
}

OpenAjax.hub.InlineContainer.prototype.getSubscriberData = function( subID )
{
    this._assertConn();
    return this._getSubscription( subID ).d;
}

OpenAjax.hub.InlineContainer.prototype.getSubscriberScope = function( subID )
{
    this._assertConn();
    return this._getSubscription( subID ).sc;
}

    /*** PRIVATE FUNCTIONS ***/

OpenAjax.hub.InlineContainer.prototype._invokeOnComplete = function( func, scope, item, success, errorCode )
{
    if ( func ) { // onComplete is optional
        try {
            scope = scope || window;
            func.call( scope, item, success, errorCode );
        } catch( e ) {
            OpenAjax.hub._debugger();
            // _invokeOnComplete is only called for client interfaces (Hub and HubClient)
            this._client._log( "caught error from onComplete callback: " + e.message );
        }
    }
}

OpenAjax.hub.InlineContainer.prototype._disconnect = function()
{
    for ( var subID in this._subs ) {
        this._hub.unsubscribeForClient( this, this._subs[subID].h );
    }
    this._subs = [];
    this._subIndex = 1; // HW FIX
    this._connected = false;
}

OpenAjax.hub.InlineContainer.prototype._assertConn = function()
{
    if ( ! this._connected ) {
        throw new Error( OpenAjax.hub.Error.Disconnected );
    }
}

OpenAjax.hub.InlineContainer.prototype._assertPubTopic = function(topic) 
{
    if ((topic == null) || (topic == "") || (topic.indexOf("*") != -1) ||
        (topic.indexOf("..") != -1) ||  (topic.charAt(0) == ".") ||
        (topic.charAt(topic.length-1) == "."))
    {
        throw new Error(OpenAjax.hub.Error.BadParameters);
    }
}

OpenAjax.hub.InlineContainer.prototype._assertSubTopic = function(topic) 
{
    if ( ! topic ) {
        throw new Error(OpenAjax.hub.Error.BadParameters);
    }
    var path = topic.split(".");
    var len = path.length;
    for (var i = 0; i < len; i++) {
        var p = path[i];
        if ((p == "") ||
           ((p.indexOf("*") != -1) && (p != "*") && (p != "**"))) {
            throw new Error(OpenAjax.hub.Error.BadParameters);
        }
        if ((p == "**") && (i < len - 1)) {
            throw new Error(OpenAjax.hub.Error.BadParameters);
        }
    }
}

OpenAjax.hub.InlineContainer.prototype._getSubscription = function( subID )
{
    var sub = this._subs[ subID ];
    if ( sub ) {
        return sub;
    }
    throw new Error( OpenAjax.hub.Error.NoSubscription );
}

////////////////////////////////////////////////////////////////////////////////

/**
 * Create a new InlineHubClient.
 * @constructor
 * @extends OpenAjax.hub.HubClient
 * 
 * @param {Object} params 
 *    Parameters used to instantiate the HubClient.
 *    Once the constructor is called, the params object belongs to the
 *    HubClient. The caller MUST not modify it.
 *    The following are the pre-defined properties on params:
 * @param {Function} params.HubClient.onSecurityAlert
 *     Called when an attempted security breach is thwarted
 * @param {Object} [params.HubClient.scope]
 *     Whenever one of the HubClient's callback functions is called,
 *     references to "this" in the callback will refer to the scope object.
 *     If not provided, the default is window.
 * @param {OpenAjax.hub.InlineContainer} params.InlineHubClient.container
 *     Specifies the InlineContainer to which this HubClient will connect
 *  
 * @throws {OpenAjax.hub.Error.BadParameters} if any of the required
 *     parameters are missing
 */
OpenAjax.hub.InlineHubClient = function( params )
{
    if ( ! params || ! params.HubClient || ! params.HubClient.onSecurityAlert ||
            ! params.InlineHubClient || ! params.InlineHubClient.container ) {
        throw new Error(OpenAjax.hub.Error.BadParameters);
    }
    
    this._params = params;
    this._onSecurityAlert = params.HubClient.onSecurityAlert;
    this._scope = params.HubClient.scope || window;
    this._container = params.InlineHubClient.container;
    
    if ( params.HubClient.log ) {
        var id = this._container.getClientID();
        var scope = this._scope;
        var logfunc = params.HubClient.log;
        this._log = function( msg ) {
            logfunc.call( scope, "InlineHubClient::" + id + ": " + msg );
        };
 	   this._doLog = true; // HW Optimization
    } else {
        this._log = function() {};
    }
}

 /*** OpenAjax.hub.HubClient interface implementation ***/

/**
 * Requests a connection to the ManagedHub, via the InlineContainer
 * associated with this InlineHubClient.
 * 
 * If the Container accepts the connection request, this HubClient's 
 * state is set to CONNECTED and the HubClient invokes the 
 * onComplete callback function.
 * 
 * If the Container refuses the connection request, the HubClient
 * invokes the onComplete callback function with an error code. 
 * The error code might, for example, indicate that the Container 
 * is being destroyed.
 * 
 * If the HubClient is already connected, calling connect will cause
 * the HubClient to immediately invoke the onComplete callback with
 * the error code OpenAjax.hub.Error.Duplicate.
 * 
 * @param {Function} [onComplete]
 *     Callback function to call when this operation completes.
 * @param {Object} [scope]  
 *     When the onComplete function is invoked, the JavaScript "this"
 *     keyword refers to this scope object.
 *     If no scope is provided, default is window.
 *    
 * In this implementation of InlineHubClient, this function operates 
 * SYNCHRONOUSLY, so the onComplete callback function is invoked before 
 * this connect function returns. Developers are cautioned that in  
 * IframeHubClient implementations, this is not the case.
 * 
 * A client application may call InlineHubClient.disconnect and then call
 * InlineHubClient.connect to reconnect to the Managed Hub.
 */
OpenAjax.hub.InlineHubClient.prototype.connect = function( onComplete, scope )
{
    this._container.connect( this, onComplete, scope );
}

/**
 * Disconnect from the ManagedHub
 * 
 * Disconnect immediately:
 * 
 * 1. Sets the HubClient's state to DISCONNECTED.
 * 2. Causes the HubClient to send a Disconnect request to the 
 * 		associated Container. 
 * 3. Ensures that the client application will receive no more
 * 		onData or onComplete callbacks associated with this 
 * 		connection, except for the disconnect function's own
 * 		onComplete callback.
 * 4. Automatically destroys all of the HubClient's subscriptions.
 * 	
 * @param {Function} [onComplete]
 *     Callback function to call when this operation completes.
 * @param {Object} [scope]  
 *     When the onComplete function is invoked, the JavaScript "this"
 *     keyword refers to the scope object.
 *     If no scope is provided, default is window.
 *    
 * In this implementation of InlineHubClient, the disconnect function operates 
 * SYNCHRONOUSLY, so the onComplete callback function is invoked before 
 * this function returns. Developers are cautioned that in IframeHubClient 
 * implementations, this is not the case.   
 * 
 * A client application is allowed to call HubClient.disconnect and 
 * then call HubClient.connect in order to reconnect.
 */
OpenAjax.hub.InlineHubClient.prototype.disconnect = function( onComplete, scope )
{
    this._container.disconnect( this, onComplete, scope );
}

OpenAjax.hub.InlineHubClient.prototype.getPartnerOrigin = function()
{
    return this._container.getPartnerOrigin();
}

OpenAjax.hub.InlineHubClient.prototype.getClientID = function()
{
    return this._container.getClientID();
}

 /*** OpenAjax.hub.Hub interface implementation ***/

/**
 * Subscribe to a topic.
 *
 * @param {String} topic
 *     A valid topic string. MAY include wildcards.
 * @param {Function} onData   
 *     Callback function that is invoked whenever an event is 
 *     published on the topic
 * @param {Object} [scope]
 *     When onData callback or onComplete callback is invoked,
 *     the JavaScript "this" keyword refers to this scope object.
 *     If no scope is provided, default is window.
 * @param {Function} [onComplete]
 *     Invoked to tell the client application whether the 
 *     subscribe operation succeeded or failed. 
 * @param {*} [subscriberData]
 *     Client application provides this data, which is handed
 *     back to the client application in the subscriberData
 *     parameter of the onData and onComplete callback functions.
 * 
 * @returns subscriptionID
 *     Identifier representing the subscription. This identifier is an 
 *     arbitrary ID string that is unique within this Hub instance
 * @type {String}
 * 
 * @throws {OpenAjax.hub.Error.Disconnected} if this Hub instance is not in CONNECTED state
 * @throws {OpenAjax.hub.Error.BadParameters} if the topic is invalid (e.g. contains an empty token)
 *
 * In this implementation of InlineHubClient, the subscribe function operates 
 * Thus, onComplete is invoked before this function returns. Developers are 
 * cautioned that in most implementations of HubClient, onComplete is invoked 
 * after this function returns.
 * 
 * If unsubscribe is called before subscribe completes, the subscription is 
 * immediately terminated, and onComplete is never invoked.
 */
OpenAjax.hub.InlineHubClient.prototype.subscribe = function( topic, onData, scope, onComplete, subscriberData )
{
    return this._container.subscribe( topic, onData, scope, onComplete, subscriberData );
}

/**
 * Publish an event on 'topic' with the given data.
 *
 * @param {String} topic
 *     A valid topic string. MUST NOT include wildcards.
 * @param {*} data
 *     Valid publishable data. To be portable across different
 *     Container implementations, this value SHOULD be serializable
 *     as JSON.
 *     
 * @throws {OpenAjax.hub.Error.Disconnected} if this Hub instance 
 *     is not in CONNECTED state
 * 
 * In this implementation, publish operates SYNCHRONOUSLY. 
 * Data will be delivered to subscribers after this function returns.
 * In most implementations, publish operates synchronously, 
 * delivering its data to the clients before this function returns.
 */
OpenAjax.hub.InlineHubClient.prototype.publish = function( topic, data )
{
    this._container.publish( topic, data );
}

/**
 * Unsubscribe from a subscription
 *
 * @param {String} subscriptionID
 *     A subscriptionID returned by InlineHubClient.prototype.subscribe()
 * @param {Function} [onComplete]
 *     Callback function invoked when unsubscribe completes
 * @param {Object} [scope]
 *     When onComplete callback function is invoked, the JavaScript "this"
 *     keyword refers to this scope object.
 *     
 * @throws {OpenAjax.hub.Error.NoSubscription} if no such subscription is found
 * 
 * To facilitate cleanup, it is possible to call unsubscribe even 
 * when the HubClient is in a DISCONNECTED state.
 * 
 * In this implementation of HubClient, this function operates SYNCHRONOUSLY. 
 * Thus, onComplete is invoked before this function returns. Developers are 
 * cautioned that in most implementations of HubClient, onComplete is invoked 
 * after this function returns.
 */
OpenAjax.hub.InlineHubClient.prototype.unsubscribe = function( subscriptionID, onComplete, scope )
{
    this._container.unsubscribe( subscriptionID, onComplete, scope );
}

OpenAjax.hub.InlineHubClient.prototype.isConnected = function()
{
    return this._container.isConnected();
}

OpenAjax.hub.InlineHubClient.prototype.getScope = function()
{
    return this._scope;
}

OpenAjax.hub.InlineHubClient.prototype.getSubscriberData = function( subID )
{
    return this._container.getSubscriberData( subID );
}

OpenAjax.hub.InlineHubClient.prototype.getSubscriberScope = function( subID )
{
    return this._container.getSubscriberScope( subID );
}

/**
 * Returns the params object associated with this Hub instance.
 * Allows mix-in code to access parameters passed into constructor that created
 * this Hub instance.
 *
 * @returns params  the params object associated with this Hub instance
 * @type {Object}
 */
OpenAjax.hub.InlineHubClient.prototype.getParameters = function()
{
    return this._params;
}
             
////////////////////////////////////////////////////////////////////////////////
 
 /*

 Copyright 2006-2009 OpenAjax Alliance

 Licensed under the Apache License, Version 2.0 (the "License"); 
 you may not use this file except in compliance with the License. 
 You may obtain a copy of the License at
 
         http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software 
 distributed under the License is distributed on an "AS IS" BASIS, 
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 See the License for the specific language governing permissions and 
 limitations under the License.
*/

if ( typeof OpenAjax === "undefined" ) {
OpenAjax = { hub: {} };
}

/**
* Create a new Iframe Container.
* @constructor
* @extends OpenAjax.hub.Container
* 
* IframeContainer implements the Container interface to provide a container
* that isolates client components into secure sandboxes by leveraging the
* isolation features provided by browser iframes.
* 
* @param {OpenAjax.hub.ManagedHub} hub
*    Managed Hub instance to which this Container belongs
* @param {String} clientID
*    A string ID that identifies a particular client of a Managed Hub. Unique
*    within the context of the ManagedHub.
* @param {Object} params  
*    Parameters used to instantiate the IframeContainer.
*    Once the constructor is called, the params object belongs exclusively to
*    the IframeContainer. The caller MUST not modify it.
*    The following are the pre-defined properties on params:
* @param {Function} params.Container.onSecurityAlert
*    Called when an attempted security breach is thwarted.  Function is defined
*    as follows:  function(container, securityAlert)
* @param {Function} [params.Container.onConnect]
*    Called when the client connects to the Managed Hub.  Function is defined
*    as follows:  function(container)
* @param {Function} [params.Container.onDisconnect]
*    Called when the client disconnects from the Managed Hub.  Function is
*    defined as follows:  function(container)
* @param {Object} [params.Container.scope]
*    Whenever one of the Container's callback functions is called, references
*    to "this" in the callback will refer to the scope object. If no scope is
*    provided, default is window.
* @param {Function} [params.Container.log]
*    Optional logger function. Would be used to log to console.log or
*    equivalent. 
* @param {Object} params.IframeContainer.parent
*    Element ID of DOM element that is to be parent of iframe
* @param {String} params.IframeContainer.uri
*    Initial Iframe URI (Container will add parameters to this URI)
* @param {String} params.IframeContainer.tunnelURI
*    URI of the tunnel iframe. Must be from the same origin as the page which
*    instantiates the IframeContainer.
* @param {Object} [params.IframeContainer.iframeAttrs]
*    Attributes to add to IFRAME DOM entity.  For example:
*              { style: { width: "100%",
*                         height: "100%" },
*                className: "some_class" }
* @param {Number} [params.IframeContainer.timeout]
*    Load timeout in milliseconds.  If not specified, defaults to 15000.  If
*    the client at params.IframeContainer.uri does not establish a connection
*    with this container in the given time, the onSecurityAlert callback is
*    called with a LoadTimeout error code.
* @param {Function} [params.IframeContainer.seed]
*    A function that returns a string that will be used to seed the
*    pseudo-random number generator, which is used to create the security
*    tokens.  An implementation of IframeContainer may choose to ignore this
*    value.
* @param {Number} [params.IframeContainer.tokenLength]
*    Length of the security tokens used when transmitting messages.  If not
*    specified, defaults to 6.  An implementation of IframeContainer may choose
*    to ignore this value.
*
* @throws {OpenAjax.hub.Error.BadParameters}   if required params are not
*          present or null
* @throws {OpenAjax.hub.Error.Duplicate}   if a Container with this clientID
*          already exists in the given Managed Hub
* @throws {OpenAjax.hub.Error.Disconnected}   if hub is not connected
*/
OpenAjax.hub.IframeContainer = function( hub, clientID, params )
{
if ( ! hub || ! clientID || ! params ||
     ! params.Container || ! params.Container.onSecurityAlert ||
     ! params.IframeContainer || ! params.IframeContainer.parent ||
     ! params.IframeContainer.uri || ! params.IframeContainer.tunnelURI ) {
 throw new Error(OpenAjax.hub.Error.BadParameters);
}

this._params = params;
this._id = clientID;

if ( window.postMessage ) {
 this._delegate = new OpenAjax.hub.IframePMContainer( this, hub, clientID, params );
} else {
 this._delegate = new OpenAjax.hub.IframeFIMContainer( this, hub, clientID, params );
}

// Create IFRAME to hold the client
this._iframe = this._createIframe( params.IframeContainer.parent, this._delegate.getURI(),
     params.IframeContainer.iframeAttrs );

hub.addContainer( this );
}

/*** OpenAjax.hub.Container interface implementation ***/

OpenAjax.hub.IframeContainer.prototype.getHub = function()
{
return this._delegate.getHub();
}

OpenAjax.hub.IframeContainer.prototype.sendToClient = function( topic, data, subscriptionID )
{
this._delegate.sendToClient( topic, data, subscriptionID );
}

OpenAjax.hub.IframeContainer.prototype.remove = function()
{
this._delegate.remove();
this._iframe.parentNode.removeChild( this._iframe );
}

OpenAjax.hub.IframeContainer.prototype.isConnected = function()
{
return this._delegate.isConnected();
}

OpenAjax.hub.IframeContainer.prototype.getClientID = function()
{
return this._id;
}

OpenAjax.hub.IframeContainer.prototype.getPartnerOrigin = function()
{
return this._delegate.getPartnerOrigin();
}

OpenAjax.hub.IframeContainer.prototype.getParameters = function()
{
return this._params;
}

/**
* Get the iframe associated with this iframe container
* 
* This function returns the iframe associated with an IframeContainer,
* allowing the Manager Application to change its size, styles, scrollbars, etc.
* 
* CAUTION: The iframe is owned exclusively by the IframeContainer. The Manager
* Application MUST NOT destroy the iframe directly. Also, if the iframe is
* hidden and disconnected, the Manager Application SHOULD NOT attempt to make
* it visible. The Container SHOULD automatically hide the iframe when it is
* disconnected; to make it visible would introduce security risks. 
* 
* @returns iframeElement
* @type {Object}
*/
OpenAjax.hub.IframeContainer.prototype.getIframe = function() 
{
return this._iframe;
}

/*** Helper Functions ***/

/**
* Return function that runs in given scope.
*
* @param {Object} toWhom  scope in which to run given function
* @param {Function} callback  function to run in given scope
* @returns {Function}
*/
OpenAjax.hub.IframeContainer.bind = function( toWhom, callback )
{
var __method = callback;
return function() {
 return __method.apply(toWhom, arguments);
}
}


/*** Private Functions ***/

OpenAjax.hub.IframeContainer.prototype._createIframe = function( parent, src, attrs )
{
var iframe = document.createElement( "iframe" );

// Add iframe attributes
if ( attrs ) {
 for ( var attr in attrs ) {
     if ( attr == "style" ) {
         for ( var style in attrs.style ) {
             iframe.style[ style ] = attrs.style[ style ];
         }
     } else {
         iframe[ attr ] = attrs[ attr ];
     }
 }
}

// initially hide IFRAME content, in order to lessen frame phishing impact
iframe.style.visibility = "hidden";

// (1) Setting the iframe src after it has been added to the DOM can cause
// problems in IE6/7.  Specifically, if the code is being executed on a page
// that was served through HTTPS, then IE6/7 will see an iframe with a blank
// src as a non-secure item and display a dialog warning the user that "this
// page contains both secure and nonsecure items."  To prevent that, we
// first set the src to a dummy value, then add the iframe to the DOM, then
// set the real src value.
// (2) Trying to fix the above issue by setting the real src before adding
// the iframe to the DOM breaks Firefox 3.x.  For some reason, when
// reloading a page that has instantiated an IframeContainer, Firefox will
// load a previously cached version of the iframe content, whose source
// contains stale URL query params or hash.  This results in errors in the
// Hub code, which is expected different values.
iframe.src = 'javascript:"<html></html>"';
parent.appendChild( iframe );
iframe.src = src;

return iframe;
}

//------------------------------------------------------------------------------

/**
* Create a new IframeHubClient.
* @constructor
* @extends OpenAjax.hub.HubClient
* 
* @param {Object} params
*    Once the constructor is called, the params object belongs to the
*    HubClient. The caller MUST not modify it.
*    The following are the pre-defined properties on params:
* @param {Function} params.HubClient.onSecurityAlert
*     Called when an attempted security breach is thwarted
* @param {Object} [params.HubClient.scope]
*     Whenever one of the HubClient's callback functions is called,
*     references to "this" in the callback will refer to the scope object.
*     If not provided, the default is window.
* @param {Function} [params.IframeHubClient.seed]
*     A function that returns a string that will be used to seed the
*     pseudo-random number generator, which is used to create the security
*     tokens.  An implementation of IframeHubClient may choose to ignore
*     this value.
* @param {Number} [params.IframeHubClient.tokenLength]
*     Length of the security tokens used when transmitting messages.  If
*     not specified, defaults to 6.  An implementation of IframeHubClient
*     may choose to ignore this value.
*     
* @throws {OpenAjax.hub.Error.BadParameters} if any of the required
*          parameters is missing, or if a parameter value is invalid in 
*          some way.
*/
OpenAjax.hub.IframeHubClient = function( params )
{
if ( ! params || ! params.HubClient || ! params.HubClient.onSecurityAlert ) {
 throw new Error( OpenAjax.hub.Error.BadParameters );
}

this._params = params;

if ( window.postMessage ) {
 this._delegate = new OpenAjax.hub.IframePMHubClient( this, params );
} else {
 this._delegate = new OpenAjax.hub.IframeFIMHubClient( this, params );
}
}

/*** OpenAjax.hub.HubClient interface implementation ***/

OpenAjax.hub.IframeHubClient.prototype.connect = function( onComplete, scope )
{
scope = scope || window;
if ( this.isConnected() ) {
 throw new Error( OpenAjax.hub.Error.Duplicate );
}

this._delegate.connect( onComplete, scope );
}

OpenAjax.hub.IframeHubClient.prototype.disconnect = function( onComplete, scope )
{
scope = scope || window;
if ( ! this.isConnected() ) {
 throw new Error( OpenAjax.hub.Error.Disconnected );
}

this._delegate.disconnect( onComplete, scope );
}

OpenAjax.hub.IframeHubClient.prototype.getPartnerOrigin = function()
{
return this._delegate.getPartnerOrigin();
}

OpenAjax.hub.IframeHubClient.prototype.getClientID = function()
{
return this._delegate.getClientID();
}

/*** OpenAjax.hub.Hub interface implementation ***/

OpenAjax.hub.IframeHubClient.prototype.subscribe = function( topic, onData, scope, onComplete, subscriberData )
{
this._assertConn();
this._assertSubTopic( topic );
if ( ! onData ) {
 throw new Error( OpenAjax.hub.Error.BadParameters );
}

scope = scope || window;
return this._delegate.subscribe( topic, onData, scope, onComplete, subscriberData );
}

OpenAjax.hub.IframeHubClient.prototype.publish = function( topic, data )
{
this._assertConn();
this._assertPubTopic( topic );
this._delegate.publish( topic, data );
}

OpenAjax.hub.IframeHubClient.prototype.unsubscribe = function( subscriptionID, onComplete, scope )
{
this._assertConn();
if ( typeof subscriptionID === "undefined" || subscriptionID == null ) {
 throw new Error( OpenAjax.hub.Error.BadParameters );
}
scope = scope || window;
this._delegate.unsubscribe( subscriptionID, onComplete, scope );
}

OpenAjax.hub.IframeHubClient.prototype.isConnected = function()
{
return this._delegate.isConnected();
}

OpenAjax.hub.IframeHubClient.prototype.getScope = function()
{
return this._delegate.getScope();
}

OpenAjax.hub.IframeHubClient.prototype.getSubscriberData = function( subscriptionID )
{
this._assertConn();
return this._delegate.getSubscriberData( subscriptionID );
}

OpenAjax.hub.IframeHubClient.prototype.getSubscriberScope = function( subscriptionID )
{
this._assertConn();
return this._delegate.getSubscriberScope( subscriptionID );
}

OpenAjax.hub.IframeHubClient.prototype.getParameters = function()
{
return this._params;
}

/*** Private Functions ***/

OpenAjax.hub.IframeHubClient.prototype._assertConn = function()
{
if ( ! this.isConnected() ) {
 throw new Error( OpenAjax.hub.Error.Disconnected );
}
}

OpenAjax.hub.IframeHubClient.prototype._assertSubTopic = function( topic )
{
if ( ! topic ) {
 throw new Error(OpenAjax.hub.Error.BadParameters);
}
var path = topic.split(".");
var len = path.length;
for (var i = 0; i < len; i++) {
 var p = path[i];
 if ((p == "") ||
    ((p.indexOf("*") != -1) && (p != "*") && (p != "**"))) {
     throw new Error(OpenAjax.hub.Error.BadParameters);
 }
 if ((p == "**") && (i < len - 1)) {
     throw new Error(OpenAjax.hub.Error.BadParameters);
 }
}
}

OpenAjax.hub.IframeHubClient.prototype._assertPubTopic = function( topic )
{
if ((topic == null) || (topic == "") || (topic.indexOf("*") != -1) ||
 (topic.indexOf("..") != -1) ||  (topic.charAt(0) == ".") ||
 (topic.charAt(topic.length-1) == "."))
{
 throw new Error(OpenAjax.hub.Error.BadParameters);
}
}

/******************************************************************************
*  PostMessage Iframe Container
*
*      Implementation of the Iframe Container which uses window.postMessage()
*      for communicating between an iframe and its parent.
******************************************************************************/

OpenAjax.hub.IframePMContainer = function( container, hub, clientID, params )
{
this._container = container;
this._hub = hub;
this._id = clientID;
this._onSecurityAlert = params.Container.onSecurityAlert;
this._onConnect = params.Container.onConnect ? params.Container.onConnect : null;
this._onDisconnect = params.Container.onDisconnect ? params.Container.onDisconnect : null;
this._scope = params.Container.scope || window;
this._uri = params.IframeContainer.uri;
this._tunnelURI = params.IframeContainer.tunnelURI;
this._timeout = params.IframeContainer.timeout || 15000;

if ( params.Container.log ) {
 var scope = this._scope;
 var logfunc = params.Container.log;
 this._log = function( msg ) {
     logfunc.call( scope, "IframeContainer::" + clientID + ": " + msg );
 };
 this._doLog = true; // HW Optimization
} else {
 this._log = function() {};
}

this._securityToken = this._generateSecurityToken( params );

this._connected = false;
this._subs = {};

// test if the postMessage impl of this browser is synchronous
if ( typeof OpenAjax.hub.IframePMContainer._pmCapabilities === "undefined" ) {
 this._testPostMessage();
}

// if postMessage is synchronous, wrap in a setTimeout
if ( OpenAjax.hub.IframePMContainer._pmCapabilities.indexOf("s") == -1 ) {
 this._postMessage = function( win, msg, origin ) {
     win.postMessage( msg, origin );
 }
} else {
 this._postMessage = function( win, msg, origin ) {
     setTimeout(
         function() {
             win.postMessage( msg, origin );
         },
         0
     );
 }
}

// register this container with the singleton message listener
if ( ! OpenAjax.hub.IframePMContainer._pmListener ) {
 OpenAjax.hub.IframePMContainer._pmListener =
         new OpenAjax.hub.IframePMContainer.PMListener();
}
// the 'internal ID' is guaranteed to be unique within the page, not just
// the ManagedHub instance
this._internalID = OpenAjax.hub.IframePMContainer._pmListener.addContainer( this );

this._startLoadTimer();
}

//communications protocol identifier
OpenAjax.hub.IframePMContainer.protocolID = "openajax-2.0";

//Singleton message listener
OpenAjax.hub.IframePMContainer._pmListener = null;

OpenAjax.hub.IframePMContainer.prototype.getHub = function() {
return this._hub;
};

OpenAjax.hub.IframePMContainer.prototype.sendToClient = function( topic, data, subscriptionID )
{
this._sendMessage( "pub", { t: topic, d: data, s: subscriptionID } );
}

OpenAjax.hub.IframePMContainer.prototype.remove = function()
{
this._disconnect();
OpenAjax.hub.IframePMContainer._pmListener.removeContainer( this._internalID );
clearTimeout( this._loadTimer );
}

OpenAjax.hub.IframePMContainer.prototype.isConnected = function()
{
return this._connected;
}

OpenAjax.hub.IframePMContainer.prototype.getPartnerOrigin = function()
{
if ( this._connected ) {
 // remove port, if it is present
 // HW Optimization
	return this._partnerOriginNoPort;
// return new RegExp( "^([a-zA-Z]+://[^:]+).*" ).exec( this._partnerOrigin )[1];
}
return null;
}

OpenAjax.hub.IframePMContainer.prototype.receiveMessage = function( event, msg )
{
// check that security token and client window origin for incoming message
// are what we expect
if ( msg.t != this._securityToken ||
     ( typeof this._partnerOrigin != "undefined" &&
       ! OpenAjax.hub.IframePMContainer.originMatches( this, event )))
{
 // security error -- incoming message is not valid; ignore
 this._invokeSecurityAlert( OpenAjax.hub.SecurityAlert.ForgedMsg );
 return;
}

if(this._doLog) { // HW Optimization
 this._log( "received message: [" + event.data + "]" );
}

switch ( msg.m ) {
 // subscribe
 case "sub":
     var errCode = "";  // empty string is success
     try {
         this._subs[ msg.p.s ] = this._hub.subscribeForClient( this._container, msg.p.t, msg.p.s );
     } catch( e ) {
         errCode = e.message;
     }
     this._sendMessage( "sub_ack", { s: msg.p.s, e: errCode } );
     break;
 
 // publish
 case "pub":
     this._hub.publishForClient( this._container, msg.p.t, msg.p.d );
     break;

 // unsubscribe
 case "uns":
     var handle = this._subs[ msg.p.s ];
     this._hub.unsubscribeForClient( this._container, handle );
     delete this._subs[ msg.p.s ];
     this._sendMessage( "uns_ack", msg.p.s );
     break;

 // connect is handled elsewhere -- see IframePMContainer.prototype.connect
 
 // disconnect
 case "dis":
     this._startLoadTimer();
     this._disconnect();
     this._sendMessage( "dis_ack", null );
     if ( this._onDisconnect ) {
         try {
             this._onDisconnect.call( this._scope, this._container );
         } catch( e ) {
             OpenAjax.hub._debugger();
             this._log( "caught error from onDisconnect callback to constructor: " + e.message );
         }
     }
     break;
}
}

/**
* Complete connection from HubClient to this Container.
*
* @param {String} origin  IframePMHubClient's window's origin
* @param {String} securityToken  Security token originally sent by Container
* @param {Object} tunnelWindow  window object reference of tunnel window
*/
OpenAjax.hub.IframePMContainer.prototype.connect = function( origin, securityToken, tunnelWindow )
{
this._log( "client connecting to container " + this._id +
     " :: origin = " + origin + " :: securityToken = " + securityToken );

// check that security token is what we expect
if ( securityToken != this._securityToken ) {
 // security error -- incoming message is not valid
 this._invokeSecurityAlert( OpenAjax.hub.SecurityAlert.ForgedMsg );
 return;
}

// set unload handler on tunnel window
var that = this;
tunnelWindow.onunload = function() {
 if ( that.isConnected() ) {
 	// Use a timer to delay the phishing message. This makes sure that
 	// page navigation does not cause phishing errors.
 	// Setting it to 1 ms is enough for it not to be triggered on
 	// regular page navigations.
     setTimeout(
         function() {
             that._invokeSecurityAlert( OpenAjax.hub.SecurityAlert.FramePhish );
         }, 1
     );
 }
};

clearTimeout( this._loadTimer );

this._iframe = this._container.getIframe();
this._iframe.style.visibility = "visible";

this._partnerOrigin = origin;
// HW Optimization
this._partnerOriginNoPort = new RegExp( "^([a-zA-Z]+://[^:]+).*" ).exec( this._partnerOrigin )[1]; // HW Optimization
// if "message" event doesn't support "origin" property, then save hostname
// (domain) also
if ( OpenAjax.hub.IframePMContainer._pmCapabilities.indexOf("d") != -1 ) {
 this._partnerDomain = new RegExp( "^.+://([^:]+).*" ).exec( this._partnerOrigin )[1];
}

this._sendMessage( "con_ack", null );
this._connected = true;
if ( this._onConnect ) {
 try {
     this._onConnect.call( this._scope, this._container );
 } catch( e ) {
     OpenAjax.hub._debugger();
     this._log( "caught error from onConnect callback to constructor: " + e.message );
 }
}
}

OpenAjax.hub.IframePMContainer.prototype.getURI = function()
{
// add the client ID and a security token as URL query params when loading
// the client iframe
var paramStr =
     "oahpv=" + encodeURIComponent( OpenAjax.hub.IframePMContainer.protocolID ) +
     "&oahi=" + encodeURIComponent( this._internalID ) +
     "&oaht=" + this._securityToken +
     "&oahu=" + encodeURIComponent( this._tunnelURI ) +
     "&oahpm=" + OpenAjax.hub.IframePMContainer._pmCapabilities;
if ( this._id !== this._internalID ) {
 paramStr += "&oahj=" + this._internalID;
}
paramStr += OpenAjax.hub.enableDebug ? "&oahd=true" : ""; // REMOVE ON BUILD

var parts = this._uri.split("#");
parts[0] = parts[0] + ((parts[0].indexOf( "?" ) != -1) ? "&" : "?") + paramStr;
if ( parts.length == 1 ) {
 return parts[0];
}
return parts[0] + "#" + parts[1];
}

/*** Helper Functions ***/

OpenAjax.hub.IframePMContainer.originMatches = function( obj, event )
{
if ( event.origin ) {
return event.origin == obj._partnerOrigin;
} else {
return event.domain == obj._partnerDomain;
}
}

/*** Private Function ***/

OpenAjax.hub.IframePMContainer.prototype._generateSecurityToken = function( params )
{
if ( ! OpenAjax.hub.IframePMContainer._prng ) {
 // create pseudo-random number generator with a default seed
 var seed = new Date().getTime() + Math.random() + document.cookie;
 OpenAjax.hub.IframePMContainer._prng = smash.crypto.newPRNG( seed );
}

if ( params.IframeContainer.seed ) {
 try {
     var extraSeed = params.IframeContainer.seed.call( this._scope );
     OpenAjax.hub.IframePMContainer._prng.addSeed( extraSeed );
 } catch( e ) {
     OpenAjax.hub._debugger();
     this._log( "caught error from 'seed' callback: " + e.message );
 }
}

var tokenLength = params.IframeContainer.tokenLength || 6;
return OpenAjax.hub.IframePMContainer._prng.nextRandomB64Str( tokenLength );
}

/**
* Some browsers (IE, Opera) have an implementation of postMessage that is
* synchronous, although HTML5 specifies that it should be asynchronous.  In
* order to make all browsers behave consistently, we run a small test to detect
* if postMessage is asynchronous or not.  If not, we wrap calls to postMessage
* in a setTimeout with a timeout of 0.
* Also, Opera's "message" event does not have an "origin" property (at least,
* it doesn't in version 9.64;  presumably, it will in version 10).  If
* event.origin does not exist, use event.domain.  The other difference is that
* while event.origin looks like <scheme>://<hostname>:<port>, event.domain
* consists only of <hostname>.
*/
OpenAjax.hub.IframePMContainer.prototype._testPostMessage = function()
{
// String identifier that specifies whether this browser's postMessage
// implementation differs from the spec:
//      contains "s" - postMessage is synchronous
//      contains "d" - "message" event does not have an "origin" property;
//                     the code looks for the "domain" property instead
OpenAjax.hub.IframePMContainer._pmCapabilities = "";

var hit = false;

function receiveMsg(event) {
 if ( event.data == "postmessage.test" ) {
     hit = true;
     if ( typeof event.origin === "undefined" ) {
         OpenAjax.hub.IframePMContainer._pmCapabilities += "d";
     }
 }
}

if ( window.addEventListener ) {
 window.addEventListener( "message", receiveMsg, false );
} else if ( window.attachEvent ) {
 window.attachEvent( "onmessage", receiveMsg );
}
window.postMessage( "postmessage.test", "*" );

// if 'hit' is true here, then postMessage is synchronous
if ( hit ) {
 OpenAjax.hub.IframePMContainer._pmCapabilities += "s";
}

if ( window.removeEventListener ) {
 window.removeEventListener( "message", receiveMsg, false );
} else {
 window.detachEvent( "onmessage", receiveMsg );
}
}

OpenAjax.hub.IframePMContainer.prototype._startLoadTimer = function()
{
var that = this;
this._loadTimer = setTimeout(
 function() {
     // don't accept any messages from client
     OpenAjax.hub.IframePMContainer._pmListener.removeContainer( that._internalID );
     // alert the security alert callback
     that._invokeSecurityAlert( OpenAjax.hub.SecurityAlert.LoadTimeout );
 },
 this._timeout
);
}

/**
* Send a string message to the associated hub client.
*
* The message is a JSON representation of the following object:
*      {
*          m: message type,
*          i: client id,
*          t: security token,
*          p: payload (depends on message type)
*      }
*
* The payload for each message type is as follows:
*      TYPE        DESCRIPTION                     PAYLOAD
*      "con_ack"    connect acknowledgment          N/A
*      "dis_ack"    disconnect acknowledgment       N/A
*      "sub_ack"    subscribe acknowledgment        { s: subscription id, e: error code (empty string if no error) }
*      "uns_ack"    unsubscribe acknowledgment      { s: subscription id }
*      "pub"        publish (i.e. sendToClient())   { t: topic, d: data, s: subscription id }
*/
OpenAjax.hub.IframePMContainer.prototype._sendMessage = function( type, payload )
{
var msg = JSON.stringify({
 m: type,
 i: this._internalID,
 t: this._securityToken,
 p: payload
});
this._postMessage( this._iframe.contentWindow, msg, this._partnerOrigin );
}

OpenAjax.hub.IframePMContainer.prototype._disconnect = function()
{
if ( this._connected ) {
 this._connected = false;
 this._iframe.style.visibility = "hidden";

 // unsubscribe from all subs
 for ( var sub in this._subs ) {
     this._hub.unsubscribeForClient( this._container, this._subs[ sub ] );
 }
 this._subs = {};
}
}

OpenAjax.hub.IframePMContainer.prototype._invokeSecurityAlert = function( errorMsg )
{
try {
 this._onSecurityAlert.call( this._scope, this._container, errorMsg );
} catch( e ) {
 OpenAjax.hub._debugger();
 this._log( "caught error from onSecurityAlert callback to constructor: " + e.message );
}
}


//------------------------------------------------------------------------------

OpenAjax.hub.IframePMContainer.PMListener = function()
{
this._containers = {};

if ( window.addEventListener ) {
 window.addEventListener( "message",
         OpenAjax.hub.IframeContainer.bind( this, this._receiveMessage ), false); 
} else if ( window.attachEvent ) {
 window.attachEvent( "onmessage",
         OpenAjax.hub.IframeContainer.bind( this, this._receiveMessage ) );
}
}

/**
* Add an IframePMContainer to listen for messages.  Returns an ID for the given
* container that is unique within the PAGE, not just the ManagedHub instance.
*/
OpenAjax.hub.IframePMContainer.PMListener.prototype.addContainer = function( container )
{
var id = container._id;
while ( this._containers[ id ] ) {
 // a client with the specified ID already exists on this page;
 // create a unique ID
 id = ((0x7fff * Math.random()) | 0).toString(16) + "_" + id;
}

this._containers[ id ] = container;
return id;
}

OpenAjax.hub.IframePMContainer.PMListener.prototype.removeContainer = function( internalID )
{
delete this._containers[ internalID ];
// XXX TODO If no more postMessage containers, remove listener?
}

/**
* Complete connection between HubClient and Container identified by "id".  This
* function is only called by the tunnel window.
*/
OpenAjax.hub.IframePMContainer.PMListener.prototype.connectFromTunnel = function( internalID, origin, securityToken, tunnelWindow )
{
if ( this._containers[ internalID ] ) {
 this._containers[ internalID ].connect( origin, securityToken, tunnelWindow );
}
}

OpenAjax.hub.IframePMContainer.PMListener.prototype._receiveMessage = function( event )
{
// If the received message isn't JSON parseable or if the resulting
// object doesn't have the structure we expect, then just return.
try {
 var msg = JSON.parse( event.data );
} catch( e ) {
 return;
}
if ( ! this._verifyMsg( msg ) ) {
 return;
}

if ( this._containers[ msg.i ] ) {
 var container = this._containers[ msg.i ].receiveMessage( event, msg );
}
}

OpenAjax.hub.IframePMContainer.PMListener.prototype._verifyMsg = function( msg )
{
return typeof msg.m == "string" && typeof msg.i == "string" &&
     "t" in msg && "p" in msg;
}

//------------------------------------------------------------------------------

OpenAjax.hub.IframePMHubClient = function( client, params )
{
// check communications protocol ID
this._checkProtocolID();

this._client = client;
this._onSecurityAlert = params.HubClient.onSecurityAlert;
this._scope = params.HubClient.scope || window;
this._id = OpenAjax.hub.IframePMHubClient.queryURLParam( "oahi" );
this._internalID = OpenAjax.hub.IframePMHubClient.queryURLParam( "oahj" ) || this._id;
this._securityToken = OpenAjax.hub.IframePMHubClient.queryURLParam( "oaht" );
this._tunnelURI = OpenAjax.hub.IframePMHubClient.queryURLParam( "oahu" );
OpenAjax.hub.IframePMContainer._pmCapabilities = OpenAjax.hub.IframePMHubClient.queryURLParam( "oahpm" );

// if any of the URL params are missing, throw WrongProtocol error
if ( ! this._id || ! this._securityToken || ! this._tunnelURI ) {
 throw new Error( OpenAjax.hub.Error.WrongProtocol );
}

if ( OpenAjax.hub.IframePMHubClient.queryURLParam("oahd") )  OpenAjax.hub.enableDebug = true; // REMOVE ON BUILD

this._partnerOrigin = new RegExp( "^([a-zA-Z]+://[^/?#]+).*" ).exec( this._tunnelURI )[1];
this._partnerOriginNoPort = new RegExp( "^([a-zA-Z]+://[^:]+).*" ).exec( this._partnerOrigin )[1]; // HW Optimization
// if "message" event doesn't support "origin" property, then save hostname
// (domain) also
if ( OpenAjax.hub.IframePMContainer._pmCapabilities.indexOf("d") != -1 ) {
 this._partnerDomain = new RegExp( "^.+://([^:]+).*" ).exec( this._partnerOrigin )[1];
}

if ( params.HubClient.log ) {
 var id = this._id;
 var scope = this._scope;
 var logfunc = params.HubClient.log;
 this._log = function( msg ) {
     logfunc.call( scope, "IframeHubClient::" + id + ": " + msg );
 };
 this._doLog = true; // HW Optimization
} else {
 this._log = function() {};
}

this._connected = false;
this._subs = {};
this._subIndex = 1; // HW FIX

// if postMessage is synchronous, wrap in a setTimeout
if ( OpenAjax.hub.IframePMContainer._pmCapabilities.indexOf("s") == -1 ) {
 this._postMessage = function( win, msg, origin ) {
     win.postMessage( msg, origin );
 }
} else {
 this._postMessage = function( win, msg, origin ) {
     setTimeout(
         function() {
             win.postMessage( msg, origin );
         },
         0
     );
 }
}
}

//communications protocol identifier
OpenAjax.hub.IframePMHubClient.protocolID = "openajax-2.0";

/*** OpenAjax.hub.HubClient interface implementation ***/

OpenAjax.hub.IframePMHubClient.prototype.connect = function( onComplete, scope )
{
if ( onComplete ) {
 this._connectOnComplete = { cb: onComplete, sc: scope };
}

// start listening for messages
this._msgListener = OpenAjax.hub.IframeContainer.bind( this, this._receiveMessage );
if ( window.addEventListener ) {
 window.addEventListener( "message", this._msgListener, false); 
} else if ( window.attachEvent ) {
 window.attachEvent( "onmessage", this._msgListener );
}

// create tunnel iframe, which will finish connection to container
var origin = window.location.protocol + "//" + window.location.host;
var iframe = document.createElement( "iframe" );
document.body.appendChild( iframe );
iframe.src = this._tunnelURI +
     (this._tunnelURI.indexOf("?") == -1 ? "?" : "&") +
     "oahj=" + encodeURIComponent( this._internalID ) +
     "&oaht=" + this._securityToken + 
     "&oaho=" + encodeURIComponent( origin );
iframe.style.position = "absolute";
iframe.style.left = iframe.style.top = "-10px";
iframe.style.height = iframe.style.width = "1px";
iframe.style.visibility = "hidden";
this._tunnelIframe = iframe;
}

OpenAjax.hub.IframePMHubClient.prototype.disconnect = function( onComplete, scope )
{
this._connected = false;
if ( onComplete ) {
 this._disconnectOnComplete = { cb: onComplete, sc: scope };
}
this._sendMessage( "dis", null );
}

OpenAjax.hub.IframePMHubClient.prototype.getPartnerOrigin = function()
{
if ( this._connected ) {
 // remove port, if it is present
 return new RegExp( "^([a-zA-Z]+://[^:]+).*" ).exec( this._partnerOrigin )[1];
}
return null;
}

OpenAjax.hub.IframePMHubClient.prototype.getClientID = function()
{
return this._id;
}

/*** OpenAjax.hub.Hub interface implementation ***/

OpenAjax.hub.IframePMHubClient.prototype.subscribe = function( topic, onData, scope, onComplete, subscriberData )
{
var subID = "" + this._subIndex++;
this._subs[ subID ] = { cb: onData, sc: scope, d: subscriberData, oc: onComplete };
this._sendMessage( "sub", { t: topic, s: subID } );
return subID;
}

OpenAjax.hub.IframePMHubClient.prototype.publish = function( topic, data )
{
this._sendMessage( "pub", { t: topic, d: data } );
}

OpenAjax.hub.IframePMHubClient.prototype.unsubscribe = function( subID, onComplete, scope )
{
// if no such subID, or in process of unsubscribing given ID, throw error
if ( ! this._subs[ subID ] || this._subs[ subID ].uns ) {
 throw new Error( OpenAjax.hub.Error.NoSubscription );
}
this._subs[ subID ].uns = { cb: onComplete, sc: scope };
this._sendMessage( "uns", { s: subID } );
}

OpenAjax.hub.IframePMHubClient.prototype.isConnected = function()
{
return this._connected;
}

OpenAjax.hub.IframePMHubClient.prototype.getScope = function()
{
return this._scope;
}

OpenAjax.hub.IframePMHubClient.prototype.getSubscriberData = function( subID )
{
var sub = this._subs[ subID ];
if ( sub ) {
 return sub.d;
}
throw new Error( OpenAjax.hub.Error.NoSubscription );
}

OpenAjax.hub.IframePMHubClient.prototype.getSubscriberScope = function( subID )
{
var sub = this._subs[ subID ];
if ( sub ) {
 return sub.sc;
}
throw new Error( OpenAjax.hub.Error.NoSubscription );
}

/*** Helper Functions ***/

OpenAjax.hub.IframePMHubClient.queryURLParam = function( param )
{
var result = new RegExp( "[\\?&]" + param + "=([^&#]*)" ).exec( window.location.search );
if ( result ) {
 return decodeURIComponent( result[1].replace( /\+/g, "%20" ) );
}
return null;
};

/*** Private Functions ***/

OpenAjax.hub.IframePMHubClient.prototype._checkProtocolID = function()
{
var partnerProtocolID = OpenAjax.hub.IframePMHubClient.queryURLParam( "oahpv" );
if ( partnerProtocolID != OpenAjax.hub.IframePMHubClient.protocolID ) {
 throw new Error( OpenAjax.hub.Error.WrongProtocol );
}
}

OpenAjax.hub.IframePMHubClient.prototype._receiveMessage = function( event )
{
// If the received message isn't JSON parseable or if the resulting
// object doesn't have the structure we expect, then just return.  This
// message might belong to some other code on the page that is also using
// postMessage for communication.
try {
 var msg = JSON.parse( event.data );
} catch( e ) {
 return;
}
if ( ! this._verifyMsg( msg ) ) {
 return;
}

// check that security token and window source for incoming message
// are what we expect
if ( msg.i != this._internalID ) {
 // this message might belong to an IframeContainer on this page
 return;
} else if ( ! OpenAjax.hub.IframePMContainer.originMatches( this, event ) ||
     msg.t != this._securityToken )
{
 // security error -- incoming message is not valid
 try{
     this._onSecurityAlert.call( this._scope, this._client,
             OpenAjax.hub.SecurityAlert.ForgedMsg );
 } catch( e ) {
     OpenAjax.hub._debugger();
     this._log( "caught error from onSecurityAlert callback to constructor: " + e.message );
 }
 return;
}

if(this._doLog) { // HW Optimization
 this._log( "received message: [" + event.data + "]" );
}

switch ( msg.m ) {
 // subscribe acknowledgement
 case "sub_ack":
     var subID = msg.p.s;
     var onComplete = this._subs[ subID ].oc;
     if ( onComplete ) {
         try {
             delete this._subs[ subID ].oc;
             var scope = this._subs[ subID ].sc;
             onComplete.call( scope, msg.p.s, msg.p.e == "", msg.p.e );
         } catch( e ) {
             OpenAjax.hub._debugger();
             this._log( "caught error from onComplete callback to HubClient.subscribe(): " + e.message );
         }
     }
     break;
 
 // publish event
 case "pub":
     var subID = msg.p.s;
     // if subscription exists and we are not in process of unsubscribing...
     if ( this._subs[ subID ] && ! this._subs[ subID ].uns ) {
         var onData = this._subs[ subID ].cb;
         var scope = this._subs[ subID ].sc;
         var subscriberData = this._subs[ subID ].d;
         try {
         	onData.call( scope, msg.p.t, msg.p.d, subscriberData );
         } catch( e ) {
             OpenAjax.hub._debugger();
             this._log( "caught error from onData callback to HubClient.subscribe(): " + e.message );
         }
     }
     break;
 
 // unsubscribe acknowledgement
 case "uns_ack":
     var subID = msg.p;
     if ( this._subs[ subID ] ) {
         var onComplete = this._subs[ subID ].uns.cb;
         if ( onComplete ) {
             try {
                 var scope = this._subs[ subID ].uns.sc;
                 onComplete.call( scope, subID, true );
             } catch( e ) {
                 OpenAjax.hub._debugger();
                 this._log( "caught error from onComplete callback to HubClient.unsubscribe(): " + e.message );
             }
         }
         delete this._subs[ subID ];
     }
     break;
 
 // connect acknowledgement
 case "con_ack":
     this._connected = true;
     if ( this._connectOnComplete ) {
         var onComplete = this._connectOnComplete.cb;
         var scope = this._connectOnComplete.sc;
         try {
             onComplete.call( scope, this._client, true );
         } catch( e ) {
             OpenAjax.hub._debugger();
             this._log( "caught error from onComplete callback to HubClient.connect(): " + e.message );
         }
         delete this._connectOnComplete;
     }
     break;
 
 // disconnect acknowledgment
 case "dis_ack":
     // stop listening for messages
     if ( window.removeEventListener ) {
         window.removeEventListener( "message", this._msgListener, false );
     } else {
         window.detachEvent( "onmessage", this._msgListener );
     }
     delete this._msgListener;
     
     this._tunnelIframe.parentNode.removeChild( this._tunnelIframe );
     delete this._tunnelIframe;
     
     if ( this._disconnectOnComplete ) {
         try {
             var onComplete = this._disconnectOnComplete.cb;
             var scope = this._disconnectOnComplete.sc;
             onComplete.call( scope, this._client, true );
         } catch( e ) {
             OpenAjax.hub._debugger();
             this._log( "caught error from onComplete callback to HubClient.disconnect(): " + e.message );
         }
         delete this._disconnectOnComplete;
     }
     break;
}
}

OpenAjax.hub.IframePMHubClient.prototype._verifyMsg = function( msg )
{
return typeof msg.m == "string" && "t" in msg && "p" in msg;
}

/**
* Send a string message to the associated container.
*
* The message is a JSON representation of the following object:
*      {
*          m: message type,
*          i: client id,
*          t: security token,
*          p: payload (depends on message type)
*      }
*
* The payload for each message type is as follows:
*      TYPE    DESCRIPTION     PAYLOAD
*      "con"    connect         N/A
*      "dis"    disconnect      N/A
*      "sub"    subscribe       { t: topic, s: subscription id }
*      "uns"    unsubscribe     { s: subscription id }
*      "pub"    publish         { t: topic, d: data }
*/
OpenAjax.hub.IframePMHubClient.prototype._sendMessage = function( type, payload )
{
var msg = JSON.stringify({
 m: type,
 i: this._internalID,
 t: this._securityToken,
 p: payload
});
this._postMessage( window.parent, msg, this._partnerOrigin );
}

////////////////////////////////////////////////////////////////////////////////
/*

Copyright 2006-2009 OpenAjax Alliance

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License.
*/


//XXX revert r231 - Revision 231 added support for having the client pass back
//both the initial URI and the current URI, which are different in the case
//or redirection.  However, in order for this to work, the final client code
//must set smash._initialClientURI to the initial URI (the URI for the page
//that did the redirection).  There isn't a clean way to do this with the
//current Hub 2.0 APIs, so I'm disabling this feature for now.  Search the code
//for "XXX revert r231".


if ( typeof OpenAjax === "undefined" ) {
OpenAjax = { hub: {} };
}

OpenAjax.hub.IframeFIMContainer = function( container, hub, clientID, params )
{
this._container = container;
this._hub = hub;
this._onSecurityAlert = params.Container.onSecurityAlert;
this._onConnect = params.Container.onConnect ? params.Container.onConnect : null;
this._onDisconnect = params.Container.onDisconnect ? params.Container.onDisconnect : null;
this._scope = params.Container.scope || window;

// XXX Need to make sure URI is absolute, or change the "clientURI!=componentURI"
// comparison in SEComm.initializationFinished (where 'clientURI' is always
// absolute, but 'componentURI' is based on params.IframeContainer.uri and
// may be relative, which makes the comparison fail)
this._clientURI = params.IframeContainer.uri;

smash.SEComm.tunnelURI = params.IframeContainer.tunnelURI;
smash._loadTimeout = params.IframeContainer.timeout || 15000;

if ( params.Container.log ) {
var scope = this._scope;
var logfunc = params.Container.log;
this._log = function( msg ) {
    logfunc.call( scope, "IframeContainer::" + clientID + ": " + msg );
};
 this._doLog = true; // HW Optimization
} else {
this._log = function() {};
}

// configurable goodbyeMessage: protects against malicious unloading of the mashup application
//if (params.goodbyeMessage != null) {
//smash._goodbyeMessage = params.goodbyeMessage;
//}
// configurable securityTokenLength
//if (params.securityTokenLength != null) {
//smash._securityTokenLength = params.securityTokenLength;
//smash._computeOtherTokenConstants();
//}

// create and configure the pseudo-random number generator, used to create
// security tokens
smash._createPRNG( this, params );

smash._ensureSingletonManager();
// the 'internal ID' is guaranteed to be unique within the page, not just
// the ManagedHub instance
this._internalID = smash._singletonManager.generateUniqueClientName( clientID );
}

OpenAjax.hub.IframeFIMContainer.prototype.getHub = function() {
return this._hub;
};

OpenAjax.hub.IframeFIMContainer.prototype.sendToClient = function( topic, data, subscriptionID )
{
smash._singletonManager.sendToClient( this._internalID, topic, data, [ subscriptionID ] );
}

OpenAjax.hub.IframeFIMContainer.prototype.remove = function()
{
/**
* Cleans up data-strucrures for communication with the given client. Needs to be called prior to unloading of the
* client to prevent false positives about 'frame phishing' attacks.
* smash.prepareForUnload(clientName: string)
*/
return smash._singletonManager.prepareForUnload( this._internalID );
}

OpenAjax.hub.IframeFIMContainer.prototype.isConnected = function()
{
return smash._singletonManager.isConnected( this._internalID );
}

OpenAjax.hub.IframeFIMContainer.prototype.getPartnerOrigin = function()
{
return smash._singletonManager.getPartnerOrigin( this._internalID );
}

OpenAjax.hub.IframeFIMContainer.prototype.getURI = function()
{
/**
* Prepares for loading of a client in a separate iframe. In addition to setting up internal data-structures,
* it updates the URI (potentially adding a fragment identifier and URI parameters). 
* The updates are necessary to pass values needed to bootstrap communication.
*
* string smash.prepareForLoad({clientName: string, uri: string, 
*  [commErrorCallback:function(clientName:string, error:string)]})
* return value of null indicates failure, a non-null return value is the updated URI
*/
var that = this;
function errorCallback( clientID, error ) {
var alertType = null;
switch( error ) {
	case smash.SecurityErrors.INVALID_TOKEN:
	case smash.SecurityErrors.TOKEN_VERIFICATION_FAILED:
	    alertType = OpenAjax.hub.SecurityAlert.ForgedMsg;
	    break;
	case smash.SecurityErrors.TUNNEL_UNLOAD:
	    alertType = OpenAjax.hub.SecurityAlert.FramePhish;
	    break;
	case smash.SecurityErrors.COMPONENT_LOAD:
	    alertType = OpenAjax.hub.SecurityAlert.LoadTimeout;
	    break;
}
try {
    that._onSecurityAlert.call( that._scope, that._container, alertType );
} catch( e ) {
    OpenAjax.hub._debugger();
    that._log( "caught error from onSecurityAlert callback to constructor: " + e.message );
}
}
var newURI = smash._singletonManager.prepareForLoad({ clientName: this._internalID,
    uri: this._clientURI, commErrorCallback: errorCallback,
    oaaContainer: this, log: this._log });

if ( newURI && OpenAjax.hub.enableDebug )  newURI += ":debug"; // REMOVE ON BUILD

return newURI;
}

//------------------------------------------------------------------------------

OpenAjax.hub.IframeFIMHubClient = function( client, params )
{
// XXX Since server redirection breaks hash communication (the server does
// not receive the fragment value, therefore the final URL does not contain
// this information), the initial message is transmitted as a URL param.
// The SMash code, though, expects messages after the hash.  So we copy
// the initial message value into the fragment.
var initialMsg = new RegExp( "[\\?&]oahm=([^&#]*)" ).exec( window.location.search );
if ( ! initialMsg ) {
throw new Error( OpenAjax.hub.Error.WrongProtocol );
}
initialMsg = initialMsg[1];

// check communications protocol ID
var partnerProtocolID = initialMsg.split( ":", 1 );
if ( partnerProtocolID[0] != smash._protocolID ) {
throw new Error( OpenAjax.hub.Error.WrongProtocol );
}
// remove protocol ID from initialMsg, since decodeMessage() doesn't
// expect it
initialMsg = initialMsg.substring( partnerProtocolID[0].length + 1 );

// copy initial message into URL fragment
var url = window.location.href + "#" + initialMsg;
window.location.replace( url );


this._client = client;
this._onSecurityAlert = params.HubClient.onSecurityAlert;
this._scope = params.HubClient.scope || window;

// pull out client id from initial message
var re = new RegExp( "\\d{3}.{" + smash._securityTokenLength + "}.{" + smash._securityTokenLength + "}\\d{3}(.*)" );
var payload = re.exec( initialMsg )[1];
var parts = payload.split(":");
var internalID = decodeURIComponent( parts[0] );
this._id = internalID.substring( internalID.indexOf("_") + 1 );

if ( parts[2] && parts[2] == "debug" )  OpenAjax.hub.enableDebug = true; // REMOVE ON BUILD

if ( params.HubClient.log ) {
var id = this._id;
var scope = this._scope;
var logfunc = params.HubClient.log;
this._log = function( msg ) {
    logfunc.call( scope, "IframeHubClient::" + id + ": " + msg );
};
 this._doLog = true; // HW Optimization
} else {
this._log = function() {};
}

this._connected = false;
this._subs = {};
this._subIndex = 1; // HW FIX

// create and configure the pseudo-random number generator, used to create
// security tokens
smash._createPRNG( this, params );

// configurable initialClientURI: only for those clients which perform URI redirection
// at client load time
//XXX revert r231
//if (params.initialClientURI) {
//smash._initialClientURI = params.initialClientURI;
//}
}

/*** OpenAjax.hub.HubClient interface implementation ***/

OpenAjax.hub.IframeFIMHubClient.prototype.connect = function( onComplete, scope )
{
if ( smash._singletonClientHub == null ) {
// allow a null clientName since the SMash provider can find it in the fragment.
smash._singletonClientHub = new smash.SEHubClient( null, this._log );
// set to be notified of security errors
var that = this;
smash._singletonClientHub.setSecurityErrorCallback( function( errorcode ) {
    if ( errorcode != smash.SecurityErrors.INVALID_TOKEN ) {
        that._log( "unknown smash security error: " + errorcode );
    }
    try {
        that._onSecurityAlert.call( that._scope, that._client, OpenAjax.hub.SecurityAlert.ForgedMsg );
    } catch( e ) {
        OpenAjax.hub._debugger();
        that._log( "caught error from onSecurityAlert callback to constructor: " + e.message );
    }
});
}

var that = this;
function cb( success, seHubClient ) {
if ( success ) {
    that._connected = true;
}
if ( onComplete ) {
    try {
        onComplete.call( scope, that._client, success );    // XXX which error to return when success == false?
    } catch( e ) {
        OpenAjax.hub._debugger();
        that._log( "caught error from onComplete callback to HubClient.connect(): " + e.message );
    }
}
}
smash._singletonClientHub.connect( cb );
}

OpenAjax.hub.IframeFIMHubClient.prototype.disconnect = function( onComplete, scope )
{
this._connected = false;
var that = this;
function cb( success, seHubClient ) {
// XXX what happens if success == false
if ( onComplete ) {
    try {
        onComplete.call( scope, that._client, success );    // XXX which error to return when success == false?
    } catch( e ) {
        OpenAjax.hub._debugger();
        that._log( "caught error from onComplete callback to HubClient.disconnect(): " + e.message );
    }
}
}
smash._singletonClientHub.disconnect( cb );
}

OpenAjax.hub.IframeFIMHubClient.prototype.getPartnerOrigin = function()
{
return smash._singletonClientHub ? smash._singletonClientHub.getPartnerOrigin() : null;
}

OpenAjax.hub.IframeFIMHubClient.prototype.getClientID = function()
{
return this._id;
}

/*** OpenAjax.hub.Hub interface implementation ***/

OpenAjax.hub.IframeFIMHubClient.prototype.subscribe = function( topic, onData, scope, onComplete, subscriberData )
{
var subID = "" + this._subIndex++;

var that = this;
var completeCallback = ! onComplete ? null :
    function ( success, subHandle, error ) {
        try {
            onComplete.call( scope, subID, success, error );
        } catch( e ) {
            OpenAjax.hub._debugger();
            that._log( "caught error from onComplete callback to HubClient.subscribe(): " + e.message );
        }
    };
function dataCallback( subHandle, topic, data ) {
try {
    onData.call( scope, topic, data, subscriberData );
} catch( e ) {
    OpenAjax.hub._debugger();
    that._log( "caught error from onData callback to HubClient.subscribe(): " + e.message );
}
}
this._subs[ subID ] = smash._singletonClientHub.subscribe( topic, completeCallback, dataCallback, scope, subscriberData );
return subID;
}

OpenAjax.hub.IframeFIMHubClient.prototype.publish = function( topic, data )
{
smash._singletonClientHub.publish( topic, data );
}

OpenAjax.hub.IframeFIMHubClient.prototype.unsubscribe = function( subID, onComplete, scope )
{
if ( ! this._subs[ subID ] ) {
throw new Error( OpenAjax.hub.Error.NoSubscription );
}
var that = this;
function cb( success, subHandle ) {
delete that._subs[ subID ];
if ( onComplete ) {
    try {
        onComplete.call( scope, subID, success/*, error*/ );
    } catch( e ) {
        OpenAjax.hub._debugger();
        that._log( "caught error from onComplete callback to HubClient.unsubscribe(): " + e.message );
    }
}
};
this._subs[ subID ].unsubscribe( cb );
}

OpenAjax.hub.IframeFIMHubClient.prototype.isConnected = function()
{
return this._connected;
}

OpenAjax.hub.IframeFIMHubClient.prototype.getScope = function()
{
return this._scope;
}

OpenAjax.hub.IframeFIMHubClient.prototype.getSubscriberData = function( subID )
{
var sub = this._subs[ subID ];
if ( sub ) {
return sub.getSubscriberData();
}
throw new Error( OpenAjax.hub.Error.NoSubscription );
}

OpenAjax.hub.IframeFIMHubClient.prototype.getSubscriberScope = function( subID )
{
var sub = this._subs[ subID ];
if ( sub ) {
return sub.getSubscriberScope();
}
throw new Error( OpenAjax.hub.Error.NoSubscription );
}


////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

if (typeof(smash) == 'undefined') { var smash = {}; }

//Ideally, should use a closure for private (and public) data and functions,
//but this was easier for the initial SMash refactoring.

smash._singletonManager = undefined; // the singleton that implements all the manager-side SPI
smash._singletonClientHub = undefined; // the singleton that implements all the client-side SPI

smash._protocolID = "openajax-2.0";

//smash._goodbyeMessage = undefined; // The goodbye message sent when unloading the mashup page. Protects against malicious unloading of the mashup application. If undefined, no message is displayed
//smash._loadTimeout = 20000; // The default timeout time during loading of a component. The lower the value the higher the security against frame-phishing but also the higer the chance of false detections.
//XXX revert r231
//smash._initialClientURI = undefined; // For use by the smash provider loaded by a client. Should only be changed from the default value if the client does URI redirection at load time. Otherwise, we will assume that the current URI was also the initial URI

//--- security token stuff ---
//configurable pseudo random number generator (prng) to use for generating the security token. 
//If not set, we use Math.random. 
//If set, the provided random number generator must support a function nextRandomB64Str(strlength:integer)
//that returns a string of length strlength, where each character is a "modified Base64 for URL" character.
//This includes A-Z, a-z, and 0-9 for the first 62 digits, like standard Base64 encoding, but
//no padding '='. And the '+', '/' characters of standard Base64 are replaced by '-', '_'.
smash._prng = undefined; 
smash._securityTokenLength = 6; // configurable security token length. If default value is not used, both manager and clients have to change it to the same value. 
smash._securityTokenOverhead = null; // the number of characters in a serialized message consumed by the security tokens
smash._computeOtherTokenConstants = function() {
smash._securityTokenOverhead = 2*smash._securityTokenLength;
smash._multiplier = Math.pow(10, smash._securityTokenLength-1);
}
smash._computeOtherTokenConstants();

smash._createPRNG = function( container, params )
{
if ( ! smash._prng ) {
// create pseudo-random number generator with a default seed
var seed = new Date().getTime() + Math.random() + document.cookie;
smash._prng = smash.crypto.newPRNG( seed );
}

var p = params.IframeContainer || params.IframeHubClient;
if ( p && p.seed ) {
try {
    var extraSeed = p.seed.call( container._scope );
    smash._prng.addSeed( extraSeed );
} catch( e ) {
    OpenAjax.hub._debugger();
    container._log( "caught error from 'seed' callback: " + e.message );
}
}
}

/**
* Randomly generates the security token which will be used to ensure message integrity.
*/
smash._keyStr = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";
smash._generateSecurityToken = function() {
var r;
if (smash._prng) 
r = smash._prng.nextRandomB64Str(smash._securityTokenLength);
else {
var r1 = Math.random(); // value in (0,1)
r = "";
// assuming one Math.random() value has enough bits for smash._securityTokenLenght
for (var i=0; i<smash._securityTokenLength; i++) {
	var r2 = r1 * 64; // get the most significant base-64 value
	var c = Math.floor(r2);
	r1 = (r2 - c); // the remaining fractional value
	r = r + smash._keyStr.charAt(c);
}
}
return r;
}

//------------------------- manager-side implementation ------------------------

/** 
* lazy creation of the manager-side singleton
*/
smash._ensureSingletonManager = function() {
if (smash._singletonManager == null)
smash._singletonManager = new smash.SEHub();
}

/**
* Constructor.
* The name SEHub is legacy. The provider on the manager-side does not implement any of the hub functionality
* other than communication.
*/
smash.SEHub = function(){
// This is used to make the object available to the private methods. This is a workaround for an error in the ECMAScript Language Specification which causes this to be set incorrectly for inner functions. See http://www.crockford.com/javascript/private.html
var that=this;
// associative array indexed by componentId. Each element is a ComponentInfo object. 
// Component is synonymous with client. componentId is the same as clientName
this.componentInfo = [];
this._subs = [];

/**
* Constructor for ComponentInfo objects
*/
function ComponentInfo(uri, eCallback) {
this.uri = uri;
//this.state = smash.SEHubConstants.START;
this.connected = false;
this.errorCallback = eCallback;
}

// create an ID that is unique within the page
this.generateUniqueClientName = function( clientName ) {
do {
    clientName = ((0x7fff * Math.random()) | 0).toString(16) + "_" + clientName;
} while ( that.componentInfo[ clientName ] );
return clientName;
}

// securityListener function registered for each component's security events
function securityListener(errorType, clientName) {
//var errorString = that.getSecurityErrorString(errorType); // get the error as a string
var ci = that.componentInfo[clientName];
if ( ci != null ) {
	var errorCallback = ci.errorCallback; // the errorCallback registered by the application
	if (errorCallback != null) { // if one was registered
//		errorCallback(clientName, errorString);
		errorCallback(clientName, errorType);
	}
}
}


/** 
* string prepareForLoad({clientName: string, uri: string, 
*  [commErrorCallback:function(clientName:string, error:string)]})
* return value of null indicates failure, a non-null return value is the updated URI
*/
this.prepareForLoad = function(params) {
var clientName = params.clientName; // componentId and clientName are the same thing in this code
var componentURI = params.uri;
if ((clientName == null) || (componentURI == null))
	return null;
if (that.componentInfo[clientName] != null) {
	return null;
}
that.componentInfo[clientName] = new ComponentInfo(componentURI, params.commErrorCallback);
that.componentInfo[clientName].seComm = new smash.SEComm(); //The SEComm library used for this component
that.componentInfo[clientName].seComm.setSecurityListener(securityListener);
that.componentInfo[clientName].oaaContainer = params.oaaContainer;
return that.componentInfo[clientName].seComm.prepareForLoad(clientName, componentURI, that, smash._loadTimeout, params.log);
}

/**
* boolean prepareForUnload(clientName: string)
*/
this.prepareForUnload = function(clientName) {
if (!that.componentInfo[clientName]) {
	// component does not exist.
	return true;
}
//// change state. pretty useless, since going to delete anyway
//that.componentInfo[clientName].state = smash.SEHubConstants.UNLOADED;
that._disconnect( clientName );
that.componentInfo[clientName].seComm.prepareForUnload();
// remove the relevant objects
delete that.componentInfo[clientName];
return true;
}

/**
* boolean isConnected(clientName:string)
*/
this.isConnected = function(clientName) {
//if ( that.componentInfo[clientName] && that.componentInfo[clientName].state == smash.SEHubConstants.LOADED )
if ( that.componentInfo[clientName] && that.componentInfo[clientName].connected ) {
    return true;
}
return false;
}

/** 
* sendToClient(clientName:string, topic: string, data:JSON|string, matchingSubs:array of string)
*/
this.sendToClient = function(clientName, topic, data, matchingSubs) {
// send to the component
if (that.isConnected(clientName)) {
	var comms = that.componentInfo[clientName].seComm;
	if (comms) {
		comms.distribute(topic, matchingSubs, data);
	}
}
}

/** Callback when component loaded */
this.componentLoaded = function(clientName, partnerURL) {
if (that.componentInfo[clientName]) {
//    that.componentInfo[clientName].state = smash.SEHubConstants.LOADED;
    that.componentInfo[clientName].connected = true;
    that.componentInfo[clientName].partnerOrigin = new RegExp( "^([a-zA-Z]+://[^:/?#]+).*" ).exec( partnerURL )[1];
    
    var oaaContainer = that.componentInfo[ clientName ].oaaContainer;
    oaaContainer._container.getIframe().style.visibility = "visible";
    if ( oaaContainer._onConnect ) {
        try {
            oaaContainer._onConnect.call( oaaContainer._scope, oaaContainer._container );
        } catch( e ) {
            OpenAjax.hub._debugger();
            oaaContainer._log( "caught error from onConnect callback to constructor: " + e.message );
        }
    }
}
}


/**
* A message received from a component
* @param componentId The component that sent the message
* @param topic 
* @param message The payload of the message (JSON|string)
*/
this.publishInternal = function(componentId, topic, message) {
if (that.componentInfo[componentId]) {
    // component exists
    var oaaContainer = that.componentInfo[ componentId ].oaaContainer;
    oaaContainer._hub.publishForClient( oaaContainer._container, topic, message );
}
}

/**
* A subscribe message received from a component
* @param componentId The component that sent the message
* @param subId The subscription id
* @param topic 
*/
this.subscribeInternal = function(componentId, subId, topic) {
var oaaContainer = that.componentInfo[ componentId ].oaaContainer;
that._subs[ subId ] = oaaContainer._hub.subscribeForClient( oaaContainer._container, topic, subId );
}

/**
* A unsubscribe message received from a component
* @param componentId The component that sent the message
* @param subId
* @returns true if unsubscribe was accepted else false
*/
this.unsubscribeInternal = function(componentId, subId) {
try {
    var handle = that._subs[ subId ];
    var oaaContainer = that.componentInfo[ componentId ].oaaContainer;
    oaaContainer._hub.unsubscribeForClient( oaaContainer._container, handle );
    return true;
} catch( e ) {}
return false;
}

this.disconnect = function( componentId )
{
that._disconnect( componentId );

var oaaContainer = that.componentInfo[ componentId ].oaaContainer;
if ( oaaContainer._onDisconnect ) {
    try {
        oaaContainer._onDisconnect.call( oaaContainer._scope, oaaContainer._container );
    } catch( e ) {
        OpenAjax.hub._debugger();
        oaaContainer._log( "caught error from onDisconnect callback to constructor: " + e.message );
    }
}
}

this._disconnect = function( componentId )
{
if ( that.componentInfo[ componentId ].connected ) {
    that.componentInfo[ componentId ].connected = false;

    // hide component iframe
    var oaaContainer = that.componentInfo[ componentId ].oaaContainer;
    oaaContainer._container.getIframe().style.visibility = "hidden";

    // unsubscribe from all subs
    for ( var sub in that._subs ) {
        oaaContainer._hub.unsubscribeForClient( oaaContainer._container, that._subs[ sub ] );
    }
    that._subs = [];
}
}

this.getPartnerOrigin = function( componentId )
{
if ( that.componentInfo[ componentId ]. connected ) {
    return that.componentInfo[ componentId ].partnerOrigin;
}
return null;
}

/**
* Converts a security error code into a readable error message.
* @param error The error code.
*/
//this.getSecurityErrorString = function(error) {
//switch (error) {
//	case smash.SecurityErrors.INVALID_TOKEN: return smash.SecurityErrors.INVALID_TOKEN_MSG;
//	case smash.SecurityErrors.TOKEN_VERIFICATION_FAILED: return smash.SecurityErrors.TOKEN_VERIFICATION_FAILED_MSG;
//	case smash.SecurityErrors.TUNNEL_UNLOAD: return smash.SecurityErrors.TUNNEL_UNLOAD_MSG;
//	case smash.SecurityErrors.COMPONENT_LOAD: return smash.SecurityErrors.COMPONENT_LOAD_MSG;
//	default: return "UNKNOWN";
//}
//}


/**
* Sets the unload function which shows the goodbye message.
*/	
//window.onunload=function(){
//if (smash._goodbyeMessage != undefined)
//	alert(smash._goodbyeMessage);
//}
}

//---------- client-side implementation ----------------------------------------

/**
* SEHubClient implementation linking the SECommClient together with the component side logic.
*/
smash.SEHubClient = function( clientName, logfunc )
{
//-------- interface implemented by connHandle in Hub 1.1. We use the SEHub instance itself
//-------- as the connHandle object for the "manager".

this.equals = function(anotherConn) { return that === anotherConn; }
this.isConnected = function() { return connected; }
this.getClientName = function() { return clientName; }

this.connect = function( callback ) {
connectCallback = function( success ) {
    if ( success ) {
        connected = true;
    }
    callback( success, that );
};
seCommClient.connect( connectCallback );
}

this.disconnect = function(callback) {
disconnectCallback = function( success ) {
    if ( success ) {
        connected = false;
	    subHandles = [];    // delete all existing subscriptions
    }
    callback( success, that );		    
};
seCommClient.disconnect();
return;
}

/**
* connHandle.subscribe(topic:string, callback:function, eventCallback:function)
* returns a subHandle object, or null if it fails immediately.
*/
this.subscribe = function(topic, callback, eventCallback, scope, subscriberData) {
// keep track of the callback so that the incomming message can be distributed correctly
var subId = (subCount + ''); // assign the subscription id - making it a string
subCount++;
subHandles[subId] = new SubHandle(subId, topic, callback, eventCallback, that, scope, subscriberData);
seCommClient.subscribe(subId, topic);
return subHandles[subId];
}
/**
* connHandle.publish(topic:string, data:JSON|string)
*/
this.publish = function(topic, data) {
seCommClient.publish(topic,data);
return true;
}
function SubHandle(subId, topic, callback, eventCallback, sehubClient, scope, subscriberData) {
var _isSubscribed = false;
var _data = subscriberData;
var _scope = scope;
var that = this;
this.getTopic = function() {
	return topic;
}
this.getConnHandle = function() {
	return sehubClient;
}
this.equals = function(anotherSubHandle) {
	if ((anotherSubHandle._getSubId != null) && (typeof anotherSubHandle._getSubId == "function")
		&& (anotherSubHandle.getConnHandle != null) && (typeof anotherSubHandle.getConnHandle == "function")) {
			if ((subId === anotherSubHandle._getSubId()) && (sehubClient === anotherSubHandle.getConnHandle()))
				return true;
		}
	return false;
}
this.isSubscribed = function() {
	return _isSubscribed;
}
this.unsubscribe = function(callback) {
	return sehubClient._unsubscribe(that, callback);
}
this.getSubscriberData = function() {
    return _data;
}
this.getSubscriberScope = function() {
    return _scope;
}
this._getSubId = function() {
	return subId;
}
this._setIsSubscribed = function(value) {
	_isSubscribed = value;
}
this._getCallback = function() {
	return callback;
}
this._getEventCallback = function() {
	return eventCallback;
}
}

this.getPartnerOrigin = function() {
if ( connected && seCommClient != null ) {
	var ptu = seCommClient.getParsedTunnelUrl();
	if ( ptu != null ) {
	    return ptu.scheme + "://" + ptu.host;
    }
}
return null;
}
//-------- end of interface implemented by connHandle in Hub 1.1.

//------- addition public interfaces not part of Hub 1.1 -----
/**
* Set a callback to find out about security errors.
* Not part of the OpenAjax Hub 1.1 standard
*/
this.setSecurityErrorCallback = function(errorcallback) {
securityErrorCallback = errorcallback;
}
//this.getManagerDomain = function() { 
//if (seCommClient != null) {
//	var ptu = seCommClient.getParsedTunnelUrl();
//	if (ptu != null) return ptu.host;
//}
//return null;
//}

//------- private stuff ------
/**
* _unsubscribe(subHandle:object, callback:function)
* returns a subHandle object, or null if it fails immediately.
*/
this._unsubscribe = function(subHandle, callback) {
var subId = subHandle._getSubId();
if ( ! subHandles[ subId ] ) {
    throw new Error( OpenAjax.hub.Error.NoSubscription );
}
subHandles[subId] = undefined;
seCommClient.unsubscribe(subId);
// no async callback as no confirmation message from manager
if (callback != null) {
	callback(true, subHandle); // function(success:boolean, subHandle:object).
}
return subHandle;
}
var securityErrorCallback = undefined; // securityErrorCallback registered by the application in this component/frame
// subscriptions: each subscription is assigned an integer id that is unique to this client
var subCount = 0;
// mapping the subscription ids to the SubHandles
var subHandles=[];
// SECommClient serving the communication between the SEHub and the SEHub client
var seCommClient=new smash.SECommClient( clientName, logfunc );
//var state = smash.SEHubConstants.LOADED; // initialize my state to LOADED.
var connected = false;
// This is used to make the object available to the private methods. This is a workaround for an error in the ECMAScript Language Specification which causes this to be set incorrectly for inner functions. See http://www.crockford.com/javascript/private.html
var that=this;
var connectCallback = null;
var disconnectCallback = null;

/**
* Processes messages received by the SECommClient
* @param message The actual message.
*/
function handleIncomingMessage(message)
{
if ( ! connected && message.type != smash.SECommMessage.CONNECT_ACK ) {
    return;
}

switch (message.type) {
case smash.SECommMessage.DISTRIBUTE:
	if ((message.additionalHeader != null) && (message.additionalHeader.s != null)) {
		var subs = message.additionalHeader.s;
		for (var i=0; i < subs.length; i++) {
			var subId = subs[i];
			if ((subId != null) && (subHandles[subId] != null)) {
				var eventCallback = subHandles[subId]._getEventCallback();
				if (eventCallback != null)
					eventCallback(subHandles[subId], message.topic, message.payload);
			}
		}
	}
	break;
case smash.SECommMessage.SUBSCRIBE_ACK:
	if (message.additionalHeader != null) {
		var subId = message.additionalHeader.subId;
		var isOk =  message.additionalHeader.isOk;
		var err = message.additionalHeader.err;
		if ((subId != null) && (isOk != null)) {
			if (subHandles[subId] != null) {
				var callback = subHandles[subId]._getCallback();
				if (isOk) {
					subHandles[subId]._setIsSubscribed(true);
					if (callback != null)
						callback(true, subHandles[subId]);
				}
				else {
					if (callback != null)
						callback(false, subHandles[subId], err);
					subHandles[subId] = undefined; // unsubscribe
				}
			}
		}
	}
	// else ignore the message
	break;
case smash.SECommMessage.CONNECT_ACK:
    connectCallback( true );
    break;
case smash.SECommMessage.DISCONNECT_ACK:
    disconnectCallback( true );
    break;
}
}
function securityListenerCallback(errorcode) {
//var errorString = getSecurityErrorString(errorcode);
if (securityErrorCallback != null) {
//	securityErrorCallback(errorString);
	securityErrorCallback(errorcode);
}
else {
	throw new Error(errorString);
}
}
//function getSecurityErrorString(error) {
//switch (error) {
//	case smash.SecurityErrors.INVALID_TOKEN: return smash.SecurityErrors.INVALID_TOKEN_MSG;
//	default: return "UNKNOWN";
//}
//}

// Override the SECommClient's received method with our own implementation
seCommClient.handleIncomingMessage = handleIncomingMessage;
seCommClient.setSecurityListener( securityListenerCallback );
}
//-----------------------------------------------------------------------------------------------
//smash.SEHubConstants = {
//
//// Constants representing states of a component.
//// Component State Machine: START -> LOADED -> UNLOADED
//
//START: 0,
//LOADED: 1,
//UNLOADED: 2
//
//};
//-----------------------------------------------------------------------------------------------
/**
* Constants representing the different types of attacks that can be detected and prevented by the library.
*/
smash.SecurityErrors = {


// This error occurs when the CommLib detects a message with a different security token than the one with wich it was initialized.
INVALID_TOKEN: 0,
//INVALID_TOKEN_MSG: "The sender of the received message could not be verified because the received security token was not correct.",
// This error occurs when the SEComm receives a different security token than the one that was sent by the SEComm during the loading of the component.
TOKEN_VERIFICATION_FAILED: 1,		
//TOKEN_VERIFICATION_FAILED_MSG: "The security token could not be verified. A different security token than the one that was sent during the loading of the component was received after loading.",
// Phishing error
TUNNEL_UNLOAD: 2,
//TUNNEL_UNLOAD_MSG: "The tunnel was unloaded without the component being unloaded by the mashup application. Frame-phishing may have occured after the component was loaded successfully.",
// Phishing error before successfull load
COMPONENT_LOAD: 3
//COMPONENT_LOAD_MSG: "A timeout occured before the communication channel between the component and the mashup application was set up correctly. Frame-phishing may have occured during the loading of the component."
};
//-----------------------------------------------------------------------------------------------
/**
* The object implementing the message serializer and deserializer for use in SEComm.
* The topic and payload are typically under application control and may contain URI reserved characters.
* These will be percent-encoded and decoded, and the application has to deal with the composition issues
* if it is passing in data or topics that are already percent-encoded. 
*/


smash.SECommMessage = function(){
// The type of the message. A string
this.type=null;
// The topic of the message. A string
this.topic=null;
// The remaining header information. A JSON object
this.additionalHeader=null;
// The payload of the message. A string
this.payload=null;
// The name used in the name value pair transmission. one character for efficiency. only use a letter or number
var typeName="y";
var topicName="t";
var additionalHeaderName = "h"; // other header information that is not handled by typeName and topicName
var payloadName="p";

/**
* Serializes the message into a string which can be transmitted over a communication channel.
* URI-encodes the topic and payload and uses "=", "&" as separators. The communication channel
* must not perform any URI-encoding as "=", "&" are not reserved for fragments. 
* If using something other than fragment messaging at the communication channel, the serialization
* may need to change.
* @returns The serialized message.
*/
this.serialize=function(){
var returnValue = typeName + "=" + this.type;
if (this.topic != null) {
	var topicString = encodeURIComponent(this.topic);
	var topicSer = "&" + topicName + "=" + topicString;  
	returnValue += topicSer;
}
if (this.additionalHeader != null) {
	var headerString = encodeURIComponent(JSON.stringify(this.additionalHeader));
	var headerSer = "&" + additionalHeaderName + "=" + headerString;  
	returnValue += headerSer;
}
if (this.payload != null) {
	var payloadString = encodeURIComponent(this.payload);
	var payloadSer = "&" + payloadName + "=" + payloadString;  
	returnValue += payloadSer;
}
return returnValue;
}

/**
* Deserializes a serialized message and initializes the objects parameters.
*/
this.deserialize=function(serializedMessage){
var messageParts = serializedMessage.split("&");
for(var i = 0; i < messageParts.length; i++){
	var nameValue = messageParts[i].split("=");
	switch(nameValue[0]){
	case typeName:
		this.type=nameValue[1];
		break;
	case topicName:
		this.topic=decodeURIComponent(nameValue[1]);
		break;
	case additionalHeaderName:
		var headerString = decodeURIComponent(nameValue[1]);
		this.additionalHeader = JSON.parse(headerString);
		break;
	case payloadName:
		this.payload=decodeURIComponent(nameValue[1]);
		break;
	}	
}
}	
}

//only use letters or numbers as characters

//CONNECT message
smash.SECommMessage.CONNECT="con";
smash.SECommMessage.CONNECT_ACK="cac";
//DISCONNECT message
smash.SECommMessage.DISCONNECT="xcon";
smash.SECommMessage.DISCONNECT_ACK="xac";
//PUBLISH message: additionalHeader is {f:"S"} or {f:"J"} representing that the payload is a string or JSON, 
//topic and payload are topic, payload of message
smash.SECommMessage.PUBLISH="pub"; 
//DISTRIBUTE message: additionalHeader is {f: string, s:[string, ...]} where f is defined as in the PUBLISH message, 
//and s representing subIds that should receive this message; topic and payload are as in PUBLISH message 
smash.SECommMessage.DISTRIBUTE="dis"; 
//SUSCRIBE message: additionalHeader is {subId: string}, payload==null, topic is subscription topic
smash.SECommMessage.SUBSCRIBE="sub"; 
//UNSUBSCRIBE message: additionalHeader is {subId: string}, topic==null, payload==null
smash.SECommMessage.UNSUBSCRIBE="uns";
//SUBCRIBE_ACK message: additionalHeader is {subId: string, isOk: boolean, err: string}, topic==null, payload == null
smash.SECommMessage.SUBSCRIBE_ACK="sac"; 

smash.SECommMessage.ERROR="err"; // TBD


//-----------------------------------------------------------------------------------------------
/**
* Definitions of exceptions used by SECom
*/
smash.SECommErrors = {};
smash.SECommErrors.tunnelNotSetError = new Error ("The tunnel URI was not set. Please set the tunnel URI.");
//smash.SECommErrors.componentNotFoundError = new Error ("The component could not be identified. Please declare the component correctly.");
//smash.SECommErrors.securityTokenNotVerifiedError = new Error (smash.SecurityErrors.TOKEN_VERIFICATION_FAILED_MSG);
//smash.SECommErrors.tunnelUnloadError = new Error (smash.SecurityErrors.TUNNEL_UNLOAD_MSG);
//smash.SECommErrors.componentLoadError = new Error (smash.SecurityErrors.COMPONENT_LOAD_MSG);

/**
* Links the SEHub and the SEHubClient together over the communication implemented by CommLib bridge
*
* TODO: Check if the component loading allows valid HTML.
* TODO: Propagate the style of the enclosing tag into the iFrame
* TODO: Check if there is a better way than polling to see if the tunnel's commLib has been registered
*/
smash.SEComm = function(){
// The timer used to delay the phishing message. This makes sure that a page navigation does not cause phishing errors.
// Setting it to 1 ms is enough for it not to be triggered on regular page navigations.
var unloadTimer=1;
// Variable storing the identifier for the setInterval if processing a registrationTimer	
var registrationTimerProcess=null;
var loadTimeout = 0;
var reconnectTimerProcess = null;
// The URI of the component being manages by this SEComm.
var componentURI=null;
// The commLib of the tunnel
var commLib=null;
// Variable storing the identifier to clear when the setInterval is called
var commLibPoll=null;
// The HTML id of the component for which this is a SEComm
var componentID=null;
// A queue for outgoing messages. This queue is used when new send requests are done while we are still sending or receiving a message.
var queueOut=[];
// Variable storing the identifier for the setInterval if processing an output queue
var queueOutProcess=null;
// Variable storing a reference to the SEHub which is managing this SEComm
var seHUB=null;
// The iframe in which the component is loaded
var myIframe = null;
// The security token used for this component
var securityTokenParent=null;
// Variable storing the callback to the security listener function
var securityListener=null;
// This is used to make the object available to the private methods. This is a workaround for an error in the ECMAScript Language Specification which causes this to be set incorrectly for inner functions. See http://www.crockford.com/javascript/private.html
var that=this;		
// keeps track of the initialization
var initialized=false;
// logging function
var logfunc = null;

/**
* Sets the callback for security errors.
* 
* @param The callback for security errors.
*/
this.setSecurityListener=function(callback){
securityListener=callback;
}

function insertURLParams( uri, params ) {
var parts = uri.split( "?" );
if ( parts.length > 1 ) {
    return parts[0] + "?" + params + "&" + parts[1];
}
parts = uri.split( "#" );
if ( parts.length > 1 ) {
    return parts[0] + "?" + params + parts[1];
}
return uri + "?" + params;
}

/**
* Prepares for loading a component into an iframe.
* @returns The modified URI
*/
this.prepareForLoad=function(componentId, frameURI, seHub, loadtimeout, logFunc)
{
logfunc = logFunc;
this.log( "Parent connecting to : " + componentId );
// Store the SEHub
seHUB=seHub;
// Store the component Id
componentID=componentId;
loadTimeout = loadtimeout;
// Check if the tunnel is set
if (smash.SEComm.tunnelURI==null)throw smash.SECommErrors.tunnelNotSetError;
// modify the URI
securityTokenParent=smash._generateSecurityToken();
// include the token twice since the child token value does not matter yet
//XXX revert r231
//componentURI = insertURLParams( frameURI, "id=" + encodeURIComponent(componentId) );
//var modifiedURIWithFragment = componentURI + "#100" + securityTokenParent + securityTokenParent + "000" + encodeURIComponent(componentId) + ":" + encodeURIComponent(smash.SEComm.tunnelURI);
// Since a server redirect does not take into account the fragment value
// (it is not transmitted by the browser to the server), the initial
// message must be sent as a URL param.
componentURI = insertURLParams( frameURI, "oahm=" + smash._protocolID + ":100" + securityTokenParent + securityTokenParent + "000" + encodeURIComponent(componentId) + ":" + encodeURIComponent(smash.SEComm.tunnelURI) );
// Make the instance available for the tunnel.
smash.SEComm.instances[componentId]=that;	
// Set a timer which detects if the component loaded successfully
// We are using an interval not to lose our evaluation context.
registrationTimerProcess=setInterval(pollForIncomingCommLibTimeout,loadTimeout);

return componentURI;
}

function pollForIncomingCommLibTimeout(){
clearInterval(registrationTimerProcess);
registrationTimerProcess = null;
//No CommLib has been registered.
if ( ! commLib ) {
     that.handleSecurityError( smash.SecurityErrors.COMPONENT_LOAD );
}
}

function reconnectTimeout() {
clearInterval( reconnectTimerProcess );
that.handleSecurityError( smash.SecurityErrors.COMPONENT_LOAD );
}

/**
* Gets the scope. Should only be used by the tunnel during INIT. 
* @returns scope (object) the scope in which the callback needs to be called.
**/
this.getScope=function(){
return this;
}

/**
* Gets the callback. Should only be used by the tunnel during INIT. 
* @param c (string) the name of the callback method	 
**/
this.getCallback=function(){
return "messageReceived";
}


/**
* Called when the initialisaiton of the library is done and processes all messages in the queue
*/
this.initializationFinished=function(tunnelCommLib, token, currentClientURI, initialClientURI, tunnelWindow)
{
this.log( "Tunnel commLib initialization finished. Processing outgoing queue. Security token: " + token );
//XXX revert r231
//// verify the security token and currentClientURI
//if ((securityTokenParent!=token) || (initialClientURI!=componentURI)) {
// verify the security token
if (securityTokenParent!=token) {
	that.handleSecurityError(smash.SecurityErrors.TOKEN_VERIFICATION_FAILED);
	return false;
}
else {
	commLib=tunnelCommLib;		
	initialized=true;
	this.log( "Token verified." );
	// register the onunload handler
	tunnelWindow.onunload=tunnelUnloadHandler;
	// switch the state to loaded in the seHUB. 
	seHUB.componentLoaded(componentID, currentClientURI);
	// process the current outgoing queue.
	while (queueOut.length>0)commLib.send(queueOut.shift());
	return true;
}
}

this.prepareForUnload = function() {
// stop all timers
if (registrationTimerProcess != null) {
	clearInterval(registrationTimerProcess);
	registrationTimerProcess = null;
}
}

function securityListenerClosure(error, componentId) {
return function() {
	securityListener(error, componentId);
}
}

this.handleSecurityError = function( error ) {
// if we have a timeout error, then overwrite initializationFinished()
// to return false by default, in order to prevent client connection
if ( error == smash.SecurityErrors.COMPONENT_LOAD ) {
    this.initializationFinished = function() {
        return false;
    }
}

if (securityListener==null){
	throw new Error (error);							
}
else{
	securityListener(error,componentID);
}
return;
}

/** 
* 
*/
function tunnelUnloadHandler(){		
if (securityListener==null){
	setTimeout("throw tunnelUnloadError;", unloadTimer);
}
else{
	setTimeout(securityListenerClosure(smash.SecurityErrors.TUNNEL_UNLOAD, componentID), unloadTimer);
}						
}

/**
* Function processing the incomming data from commLib
*
* @param message The message containing the incomming data
*/
this.messageReceived=function (message){
var msg=new smash.SECommMessage();
msg.deserialize(message);
switch(msg.type){
case smash.SECommMessage.PUBLISH:
	if (msg.additionalHeader != null) {
		var payload = msg.payload;
		if (msg.additionalHeader.f == "J")
			payload = JSON.parse(msg.payload);
		seHUB.publishInternal(componentID, msg.topic, payload);
	} // else no additionalHeader defining the payload format. hence ignore the message
	break;
case smash.SECommMessage.SUBSCRIBE:
	if (msg.additionalHeader != null)  {
	    var isOk = true;
	    var errMsg = "";
	    try {
		    seHUB.subscribeInternal(componentID, msg.additionalHeader.subId, msg.topic);
	    } catch( e ) {
	        isOk = false;
	        errMsg = e.message;
	    }
		var msgack = new smash.SECommMessage();
		msgack.type = smash.SECommMessage.SUBSCRIBE_ACK;
		msgack.additionalHeader={subId: msg.additionalHeader.subId, isOk: isOk, err: errMsg};
		send(msgack.serialize());
	}
	break;
case smash.SECommMessage.UNSUBSCRIBE:
	if (msg.additionalHeader != null)
		seHUB.unsubscribeInternal(componentID, msg.additionalHeader.subId);
	break;
case smash.SECommMessage.CONNECT:
    clearInterval( reconnectTimerProcess );
    // switch the state to loaded in the seHUB. 
	seHUB.componentLoaded( componentID, msg.payload );
    // send acknowledgement
    var msg = new smash.SECommMessage();
    msg.type = smash.SECommMessage.CONNECT_ACK;
    send( msg.serialize() );
	break;
case smash.SECommMessage.DISCONNECT:
    seHUB.disconnect( componentID );
    // Set a timer which detects if the component reloaded
	// We are using an interval not to lose our evaluation context.
	reconnectTimerProcess = setInterval( reconnectTimeout, loadTimeout );
    // send acknowledgement
    var msg = new smash.SECommMessage();
    msg.type = smash.SECommMessage.DISCONNECT_ACK;
    send( msg.serialize() );
    break;
}
}
/**
* Sends a published message to the partner component
*/
this.distribute=function(topic, matchingSubs, payload){
var msg=new smash.SECommMessage();
msg.type=smash.SECommMessage.DISTRIBUTE;
msg.topic=topic;
msg.additionalHeader = {s: matchingSubs};
if ((typeof payload) == "string") {
	msg.additionalHeader.f = "S";
	msg.payload=payload;
}
else {
	msg.additionalHeader.f = "J";
	msg.payload = JSON.stringify(payload);
}	
send(msg.serialize());
}

function send(message) {
// Queue the message if sending or if there is no communication partner yet
if (initialized==false){
	queueOut.push(message);
}
else{
	commLib.send(message);
}
}

this.log = function( msg )
{
logfunc( msg );
}
}

//Static array which contains the list of the currently loaded instances. The array is indexed by the url of the child component. 
smash.SEComm.instances=[];

//-----------------------------------------------------------------------------------------------

/**
* SEHubClient implementation linking the SEComm together with the component side logic.
*/
smash.SECommClient = function( clientName, logfunc )
{
// Storing the CommLib used for communicating
var controllers=[];
controllers["child"]=this;
var commLib=new smash.CommLib(true, controllers, clientName);
// This is used to make the object available to the private methods. This is a workaround for an error in the ECMAScript Language Specification which causes this to be set incorrectly for inner functions. See http://www.crockford.com/javascript/private.html
var that=this;
// A queue for outgoing messages. This queue is used when new send requests are done while we are still sending or receiving a message.
var queueOut=[];
// keeps track of the initialization
var initialized=false;
var securityListener=null;
var jsonPayloadHeader = {f: "J"};
var stringPayloadHeader = {f: "S"};
var parsedTunnelUrl = null;	
/**
* Publishes a message to a certain topic
* @param topic string
* @param data JSON|string
*/
this.publish=function(topic, data){
var msg=new smash.SECommMessage();
msg.type=smash.SECommMessage.PUBLISH;
msg.topic=topic;
if ((typeof data) == "string") {
	msg.additionalHeader = stringPayloadHeader;
	msg.payload=data;
}
else {
	msg.additionalHeader = jsonPayloadHeader;
	msg.payload = JSON.stringify(data);
}	
send(msg.serialize());
}

/**
* subscribes to a certain topic
*/
this.subscribe=function(subId, topic){
var msg=new smash.SECommMessage();
msg.type=smash.SECommMessage.SUBSCRIBE;
msg.topic=topic;
msg.additionalHeader = {subId: subId};
send(msg.serialize());
}

this.connect = function( callback ) {
if ( initialized ) {
    var msg = new smash.SECommMessage();
    msg.type = smash.SECommMessage.CONNECT;
    msg.payload = window.location.href.split("#")[0];
    send( msg.serialize() );
    return;
}
connectCallback = callback;
}

this.disconnect = function() {
var msg = new smash.SECommMessage();
msg.type = smash.SECommMessage.DISCONNECT;
send( msg.serialize() );
}

/**
* Called when the initialisaiton of the library is done and processes all messages in the queue
*/
this.initializationFinished=function(tunnelUrl)
{
this.log( "Initialization finished. Processing outgoing queue." );
parsedTunnelUrl = new ParsedUrl(tunnelUrl);

initialized=true;
connectCallback( true );
while (queueOut.length>0)commLib.send(queueOut.shift());
}
this.getParsedTunnelUrl=function() { return parsedTunnelUrl; }

var _regex = new RegExp("^((http|https):)?(//([^/?#:]*))?(:([0-9]*))?([^?#]*)(\\?([^#]*))?");
function ParsedUrl(url) {
var matchedurl = url.match(_regex);
this.scheme = (matchedurl[2] == "") ? null : matchedurl[2];
this.host = (matchedurl[4] == "") ? null : matchedurl[4];
this.port = (matchedurl[6] == "") ? null : matchedurl[6];
this.path = (matchedurl[7] == "") ? null : matchedurl[7];
this.query = (matchedurl[8] == "") ? null : matchedurl[8];
}

/**
* unsubscribes
*/
this.unsubscribe=function(subId){
var msg=new smash.SECommMessage();
msg.type=smash.SECommMessage.UNSUBSCRIBE;
msg.additionalHeader={subId: subId};
send(msg.serialize());
}

function send(message) {
// Queue the message if sending or if there is no communication partner yet
if (initialized==false){
	queueOut.push(message);
}
else{
	commLib.send(message);
}
}


/**
* Function processing the incomming data from commLib
*
* @param message The message containing the incomming data
*/
this.messageReceived=function (message){
var msg=new smash.SECommMessage();
msg.deserialize(message);
// parse the JSON payload
if (msg.type == smash.SECommMessage.DISTRIBUTE) {
	var header = msg.additionalHeader;
	if ((header != null) && (header.f == "J"))
		msg.payload = JSON.parse(msg.payload);
} 
//For now, pass all messages to handleIncomingMessage()		
//if ((msg.type == smash.SECommMessage.DISTRIBUTE) || (msg.type == smash.SECommMessage.SUBSCRIBE_ACK))
	that.handleIncomingMessage(msg);
}


this.handleSecurityError=function (error){
if (securityListener==null){
	throw new Error (error);							
}
else{
	securityListener( error, clientName );
}
return;
}


/**
* Sets the callback for security errors.
* 
* @param The callback for security errors.
*/
this.setSecurityListener=function(callback){
securityListener=callback;
}

/**
* This method is the location for the callback to the SECommClient library.
* The application using this library overrides this method with its own implementation.
* HACK: this is terrible from a layering perspective. Ideally all message formatting details, such
* as header formats should be handled at this layer alone.
* The default behavior is to alert a message.
*
* @param message The actual message.
*/
this.handleIncomingMessage=function(message){
alert("SECommClient\n\nTopic: " + message.topic + "\n\nPayload: " + message.payload);
}

this.log = function( msg ) {
logfunc( msg );
}
}

/**
* Provides the low level communication layer.
* @param child (boolean) indicating if this is a child iframe or not.  
* @param controllers (object []) an array indexed by the clientName of objects implementing the controller interface.
* @param clientName - only explicitly passed for the child iframe
* 
* controller.messageReceived - called when the commlib recieves an incomming message.
* controller.initializationFinished - called when the commlib finished its initialzation.
* controller.handleSecurityError - called when a security error occurs.
* 
*/
smash.CommLib=function(child, controllers, clientName){
/**BEGIN of communcation protocol **/       
/*
Message format:
| Message Type | Message Sequence Number | Security Token Parent | Security Token Child | ACK          | ACK Message Sequence Number   | Payload         |
| 1 character  | 2 characters            |  x characters         | x characters         | 1 character  | 2 characters                  | varable length  | 
*/
// Init message payload=communication partner url
var INIT="1";		
// An ack message without any payload. The reciever is not supposed to ack this message therefore the message sequence number will be 00.
var ACK="2";		
// The part message indicates that this is a message that needed to be split up. It will contain the payload of a part of the total message.
var PART="3";
// The end message indicates that this is the last part of a split up message. The full message has arrived after processing this message.
var END="4";		

/** END of communcation protocol **/
// This is used to make the object available to the private methods. This is a workaround for an error in the ECMAScript Language Specification which causes this to be set incorrectly for inner functions. See http://www.crockford.com/javascript/private.html
var that=this;		
// polling and queue processing interval
var interval=100;
// The maximul length of a URL. If the message is longer it will be split into different parts.
var urlLimit = 4000;
// Protocol overhead excluding security token overhead
var protocolOverhead=6;
// Need to do an acknowledgement
var ack=0;
// Raw incoming data
var currentHash=null;
// The newly decoded incoming message
var messageIn=null;
// The last decoded incoming message
var previousIn=null;
// The currently transmitted message
var messageOut=null;
// The previously transmitted message
var previousOut=null;		
// The url of the  partner
var partnerURL=null;
// The window object of the partner
var partnerWindow=null;
// A queue for outgoing messages. This queue is used when new send requests are done while we are still sending or recieving a message.
var queueOut=[];
// Storing the last sent message number
var msn=00;
// Buffer for storing the incoming message parts
var messageBuffer="";
// Variable storing the timerId of the message timer.
var timerId=null;
// Two security tokens - One created by the parent frame (the manager) and one by the child frame (the client)
var securityTokenParent=null;
var securityTokenChild=null;
// 
var controller = null;
var logQ = [];

/**
* Sends a message to the communication partner
* @param message (string) the message that needs to be delivered to the communication partner
*/
this.send=function(message){
// check if we are properly initialized
if (partnerURL==null){
	log( "Trying to send without proper initialization. Message will be discarded. " +  message );
	return;
}
log( "Sending: " + message );
// URL encode the message
// var encodedMessage=encodeURIComponent(message);
var encodedMessage=message;
// determine the payload size
var payloadLength=urlLimit-protocolOverhead-smash._securityTokenOverhead-partnerURL.length;
// DEBUG LARGE MESSAGES 
//if(oah_ifr_debug)payloadLength=1;
// Split up into separate messages if necessary
var currentMessage=encodedMessage;
while (currentMessage.length>0){
	// split up and put in output queue
	var part=currentMessage.substr(0,payloadLength);
	currentMessage=currentMessage.substr(payloadLength);
	if (currentMessage==0){
		queueOut.push({type: END, payload: part});
	}
	else{
		queueOut.push({type: PART, payload: part});
	}
}
}

/**
* The timer triggering the flow of messages through the system.
*/
function messageTimer(){
// check if there is a new message
if(checkMessage()){
	// check if it can be decoded properly
	if (decodeMessage()){
		// check if it is conform the security requirements
		if (checkSecurity()){
			// process it
			processMessage();
		}					
	}				
}
// Only sent if an ack was received for the last transmitted message.
if (checkAck()){
	// send anything that might be in the out queue
	sendMessage();
}
}

/**
* Returns true if the previously transmitted message was acknowledged.
* 
* Possible exception situations to take into account: 
* - One of the parties takes two turns in a row.
*   p   p   c   
* c p1  - 
* p         p1'
* 
*   c   p   p   c
* c     ac1 p1  
* p c1          p1'
* 
*/
function checkAck(){
// No ack is expected for an ack.
if (previousOut.type==ACK)return true;
// Ack is received. 
if ((previousOut.msn==messageIn.ackMsn) && (messageIn.ack==1)) return true;
// Wait for the ack to arrive.
log( "Waiting for ACK : " + previousOut.msn );
return false;
}

/**
* Helper method providing a new  message sequence number
* @returns (string) the new sequence number
*/
function getNewMsn(){
msn++;
if (msn==100) msn=0;
if (msn<10) return "0" + msn;
return "" + msn;			
}

/**
* Checks the information after the hash to see if there is a new incomming message.
*/
function checkMessage(){
//Can't use location.hash because at least Firefox does a decodeURIComponent on it.
var urlParts = window.location.href.split("#");
if(urlParts.length == 2){
	var newHash = urlParts[1];
	if(newHash!="" && newHash != currentHash){
		currentHash = newHash;
		return true;
	}
}
return false;
}

/**
* Decodes an incomming message and checks to see if it is syntactially valid.
*/		
function decodeMessage() {
//new RegExp( "(\\d)(\\d{2})(.{" + smash._securityTokenLength + "})(.{" + smash._securityTokenLength + "})(\\d)(\\d{2})(.*)" )
var type=currentHash.substr(0,1);
var msn=currentHash.substr(1,2);
var nextStart = 3;
var tokenParent=currentHash.substr(nextStart,smash._securityTokenLength);
nextStart += smash._securityTokenLength;
var tokenChild=currentHash.substr(nextStart,smash._securityTokenLength);
nextStart += smash._securityTokenLength;
var ack=currentHash.substr(nextStart,1);
nextStart += 1;
var ackMsn=currentHash.substr(nextStart,2);
nextStart += 2;
// The payload needs to stay raw since the uri decoding needs to happen on the concatenated data in case of a large message
var payload=currentHash.substr(nextStart);
log( "In : Type: " + type + " msn: " + msn + " tokenParent: " + tokenParent + " tokenChild: " + tokenChild + " ack: " + ack + " msn: " + ackMsn + " payload: " + payload );
messageIn={type: type, msn: msn, tokenParent: tokenParent, tokenChild: tokenChild, ack: ack, ackMsn: ackMsn, payload: payload};
return true;
}

/**
* Check if there have been any security breaches in the message.
*/				
function checkSecurity(){			
// Check the security tokens
if (messageIn.type!=INIT && (messageIn.tokenParent!=securityTokenParent || messageIn.tokenChild!=securityTokenChild)){
	log( "Security token error: Invalid security token received. The message will be discarded." );
	handleSecurityError(smash.SecurityErrors.INVALID_TOKEN);
	return false;
}		
// Attacks should never pass the security check. Code below is to debug the implementation.
//if(oah_ifr_debug){
//	if (messageIn.type!=INIT && messageIn.type!=ACK && messageIn.type!=PART && messageIn.type!=END){
//		if(oah_ifr_debug)debug("Syntax error: Message Type. The message will be discarded.");
//		return false;
//	}
//	if (!(messageIn.msn>=0 && messageIn.msn<=99)){
//		if(oah_ifr_debug)debug("Syntax error: Message Sequence Number. The message will be discarded.");
//		return false;
//	}
//	if (!(messageIn.ack==0 || messageIn.ack==1)){
//		if(oah_ifr_debug)debug("Syntax error: ACK. The message will be discarded.");
//		return false;
//	}
//	if (!(messageIn.ackMsn>=0 && messageIn.ackMsn<=99)){
//		if(oah_ifr_debug)debug("Syntax error: ACK Message Sequence Number. The message will be discarded.");
//		return false;
//	}
//}
return true;
}

/**
* Process the incoming message.
*/						
function processMessage(){
ack=1;
// The child is initialized as soon as there is an ack for the init message sent by the child.
if (messageIn.type!=INIT && child && previousOut.type==INIT && messageIn.ack=="1" && previousOut.msn==messageIn.ackMsn) {
    controller.initializationFinished(partnerURL);
}
					
// Call the actual processing functions
switch(messageIn.type){
	case INIT:
		processInit();
		break;
	case ACK:
		processAck();
		break;
	case PART:
		processPart();
		break;
	case END:
		processEnd();
		break;					
}
// Set the processed message as the previousIn message
previousIn=messageIn;		
}

/**
* Implementation of the INIT message type
**/
function processInit(){
var parts = messageIn.payload.split(":");
var cname = decodeURIComponent(parts[0]);
partnerURL=decodeURIComponent(parts[1]);
securityTokenParent=messageIn.tokenParent;
securityTokenChild=messageIn.tokenChild;
// Initialize a component
if (child){
	if (clientName != null) cname = clientName; // override what is read from the URL
	// generate a real security token for the child
	securityTokenChild = smash._generateSecurityToken();
	// GUI which will be used to name the iFrame tunnel.
	var tunnelGUID="3827816c-f3b1-11db-8314-0800200c9a66";
	// Generate the hidden iframe for communicating
	var iframe = document.createElement("iframe");
	var currentClientURI = encodeURIComponent(window.location.href.split("#")[0]);
	var initialClientURI = currentClientURI;
//	if (smash._initialClientURI) {
//		initialClientURI = encodeURIComponent(smash._initialClientURI);
//	}
	var initpayload = encodeURIComponent(cname) + ":" + currentClientURI + ":" + initialClientURI;

	// sending an ack for msn "00" to the tunnel, since have processed the INIT message,
	// and so that the INIT message the component is sending to the tunnel will result
	// in an ack to be sent back.
    // XXX Since server redirection breaks hash communication (the server does
    //  not receive the fragment value, therefore the final URL does not contain
    //  this information), the initial message is transmitted as a URL param.
	partnerURL += (partnerURL.indexOf("?") != -1 ? "&" : "?") + "oahm=100" + securityTokenParent + securityTokenChild + "100" + initpayload;
	iframe.src = partnerURL;
	iframe.name=tunnelGUID;
	iframe.id=tunnelGUID;
	document.body.appendChild(iframe);
	iframe.style.position = "absolute";
	iframe.style.left = iframe.style.top = "-10px";
	iframe.style.height = iframe.style.width = "1px";
	iframe.style.visibility = "hidden";
	// We do not send an ack directly to the parent frame since it is impossible to directly communicate with it in IE7
	// The ack is done indirectly when the registerTunnelCommLib is done
	ack=0;
	// set up the partner window
	partnerWindow=window.frames[tunnelGUID];
	// store the last sent message - will be used to detect intialization and for detecting security breaches
	previousOut={type: INIT, msn: "00", tokenParent: securityTokenParent, tokenChild: securityTokenChild, ack: "0", ackMsn: "00", payload: initpayload}; // only using type and msn of previousOut. presumably the rest is for FDK's retransmit stuff? should get rid of this complexity
	// set the controller for this component
	controller=controllers["child"];
}
// Initialize a tunnel
else{
	var initialClientURI = decodeURIComponent(parts[2]);
	// set up the partner window
	partnerWindow=window.parent;
	// set the controller for this component
	controller=controllers[cname];
	var success = controller.initializationFinished(that, securityTokenParent, partnerURL, initialClientURI, window);
	if (!success) ack = 0; // don't send an ack signalling the completion of connection setup.
	// store the last sent message - will be used to detect intialization and for detecting security breaches
	previousOut={type: INIT, msn: "00", tokenParent: securityTokenParent, tokenChild: securityTokenChild, ack: "0", ackMsn: "00", payload: (encodeURIComponent(cname) + ":" + encodeURIComponent(window.location.href.split("#")[0]))}; // only using type and msn of previousOut. presumably the rest is for FDK's retransmit stuff? should get rid of this complexity				
}
if (partnerWindow==null) {
	log( "Init failed." );
}
}	
	
/**
* Implementation of the ACK message type
**/
function processAck(){
// do not ack an ack
ack=0;
}

/**
* Implementation of the PART message type
**/
function processPart(){
// Process message
messageBuffer+=messageIn.payload;
}		

/**
* Implementation the END message type
**/
function processEnd(){
// Process message
messageBuffer+=messageIn.payload;
// messageBuffer=decodeURIComponent(messageBuffer);
log( "Received: " + messageBuffer );
controller.messageReceived(messageBuffer);
messageBuffer="";
}

/**
* Send a reply to the incoming message.
*/								
function sendMessage(){						
// If there is nothing in the queue and an ack needs to be sent put the ack on the queue;
if (queueOut.length==0 && ack==1){
	// The correct values will be filled in later. Just push a clean ack message
	queueOut.push({type: ACK, payload: ""});
}
// Process the output queue
if (queueOut.length!=0){
	messageOut=queueOut.shift();
	// Fill in the security token
	messageOut.tokenParent=securityTokenParent;
	messageOut.tokenChild=securityTokenChild;
	// Get a new sequence number
	messageOut.msn=getNewMsn();
	// Fill in the right ack values 
	// The protocol keeps acking the last received message to ensure that there are no 
	// problems with overwriting a pure ack message. Which could happen because there is 
	// no waiting for an ack of an ack.			
	messageOut.ack="1";
	messageOut.ackMsn=previousIn.msn;
	// turn of the ack
	ack=0;			
	writeToPartnerWindow();
}
}				

/**
* Writes the message to the partner window's fragment id
**/		
function writeToPartnerWindow(){			
var url = partnerURL + "#" + messageOut.type + messageOut.msn + messageOut.tokenParent + messageOut.tokenChild + messageOut.ack + messageOut.ackMsn + messageOut.payload;
partnerWindow.location.replace(url);
previousOut=messageOut;
log( "Out: Type: " + messageOut.type + " msn: " + messageOut.msn + " tokenParent: " + messageOut.tokenParent + " tokenChild: " + messageOut.tokenChild + " ack: " + messageOut.ack + " msn: " + messageOut.ackMsn + " payload: " + messageOut.payload );
}		

/**
* Default handler of the security listener. If a security error occurs, the CommLib is switched off. And communication is no longer possible.
* 
*/
function handleSecurityError(error){
// Stop the communication
clearInterval(timerId);	
// If there	is a securityListener inform the controller of what happened.
controller.handleSecurityError(error);
}

function log( msg )
{
if ( controller ) {
    while ( logQ.length > 0 ) {
        controller.log( logQ.shift() );
    }
    controller.log( msg );
} else {
    logQ.push( msg );
}
}

// Start listening for incoming messages
timerId=setInterval(messageTimer, interval);
};

////////////////////////////////////////////////////////////////////////////////
/*

Copyright 2006-2009 OpenAjax Alliance

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License.
*/
//SMASH.CRYPTO
//
//Small library containing some minimal crypto functionality for a
//- a hash-function: SHA-1 (see FIPS PUB 180-2 for definition)
//BigEndianWord[5] <- smash.crypto.sha1( BigEndianWord[*] dataWA, int lenInBits)
//
//- a message authentication code (MAC): HMAC-SHA-1 (RFC2104/2202)
//BigEndianWord[5] <- smash.crypto.hmac_sha1(
//                    BigEndianWord[3-16] keyWA, 
//                    Ascii or Unicode string dataS,
// 		 		       int chrsz (8 for Asci/16 for Unicode)
//
//- pseudo-random number generator (PRNG): HMAC-SHA-1 in counter mode, following
//Barak & Halevi, An architecture for robust pseudo-random generation and applications to /dev/random, CCS 2005
//rngObj <- smash.crypto.newPRNG( String[>=12] seedS)
//where rngObj has methods
//addSeed(String seed)
//BigEndianWord[len] <- nextRandomOctets(int len)
//Base64-String[len] <- nextRandomB64Str(int len)
//Note: HMAC-SHA1 in counter-mode does not provide forward-security on corruption. 
// However, the PRNG state is kept inside a closure. So if somebody can break the closure, he probably could
// break a whole lot more and forward-security of the prng is not the highest of concerns anymore :-)

if (typeof(smash) == 'undefined') { var smash = {}; }

smash.crypto = {

// Some utilities
// convert a string to an array of big-endian words
'strToWA': function (/* Ascii or Unicode string */ str, /* int 8 for Asci/16 for Unicode */ chrsz){
var bin = Array();
var mask = (1 << chrsz) - 1;
for(var i = 0; i < str.length * chrsz; i += chrsz)
bin[i>>5] |= (str.charCodeAt(i / chrsz) & mask) << (32 - chrsz - i%32);
return bin;
},


// MAC
'hmac_sha1' : function(
/* BigEndianWord[3-16]*/             keyWA,
/* Ascii or Unicode string */       dataS,
/* int 8 for Asci/16 for Unicode */ chrsz)
{
// write our own hmac derived from paj's so we do not have to do constant key conversions and length checking ...
var ipad = Array(16), opad = Array(16);
for(var i = 0; i < 16; i++) {
ipad[i] = keyWA[i] ^ 0x36363636;
opad[i] = keyWA[i] ^ 0x5C5C5C5C;
}

var hash = this.sha1( ipad.concat(this.strToWA(dataS, chrsz)), 512 + dataS.length * chrsz);
return     this.sha1( opad.concat(hash), 512 + 160);
},


// PRNG factory method
// see below 'addSeed', 'nextRandomOctets' & 'nextRandomB64Octets' for public methods of returnd prng object
'newPRNG' : function (/* String[>=12] */ seedS) {
that = this;

// parameter checking
// We cannot really verify entropy but obviously the string must have at least a minimal length to have enough entropy
// However, a 2^80 security seems ok, so we check only that at least 12 chars assuming somewhat random ASCII
if ( (typeof seedS != 'string') || (seedS.length < 12) ) {
alert("WARNING: Seed length too short ...");
}

// constants
var __refresh_keyWA = [ 0xA999, 0x3E36, 0x4706, 0x816A,
	 		 		     0x2571, 0x7850, 0xC26C, 0x9CD0,
	 		 		     0xBA3E, 0xD89D, 0x1233, 0x9525,
	 		 		     0xff3C, 0x1A83, 0xD491, 0xFF15 ]; // some random key for refresh ...

// internal state
var _keyWA = []; // BigEndianWord[5]
var _cnt = 0;  // int

function extract(seedS) {
return that.hmac_sha1(__refresh_keyWA, seedS, 8);
}

function refresh(seedS) {
// HMAC-SHA1 is not ideal, Rijndal 256bit block/key in CBC mode with fixed key might be better
// but to limit the primitives and given that we anyway have only limited entropy in practise
// this seems good enough
var uniformSeedWA = extract(seedS);
for(var i = 0; i < 5; i++) {
_keyWA[i] ^= uniformSeedWA[i];
}
}

// inital state seeding
refresh(seedS);

// public methods
return {
// Mix some additional seed into the PRNG state
'addSeed'         : function (/* String */ seed) {
// no parameter checking. Any added entropy should be fine ...
refresh(seed);
},


// Get an array of len random octets
'nextRandomOctets' : /* BigEndianWord[len] <- */ function (/* int */ len) {
 var randOctets = [];
 while (len > 0) {
   _cnt+=1;
   var nextBlock = that.hmac_sha1(_keyWA, (_cnt).toString(16), 8);
   for (i=0; (i < 20) & (len > 0); i++, len--) {
     randOctets.push( (nextBlock[i>>2] >> (i % 4) ) % 256);
   }
   // Note: if len was not a multiple 20, some random octets are ignored here but who cares ..
 }
 return randOctets;
},


// Get a random string of Base64-like (see below) chars of length len
// Note: there is a slightly non-standard Base64 with no padding and '-' and '_' for '+' and '/', respectively
'nextRandomB64Str' : /* Base64-String <- */ function (/* int */ len) {
 var b64StrMap = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";

 var randOctets = this.nextRandomOctets(len);
 var randB64Str = '';
 for (var i=0; i < len; i++) {
   randB64Str += b64StrMap.charAt(randOctets[i] & 0x3F);
 }
return randB64Str;
}

}
},


// Digest function:
// BigEndianWord[5] <- sha1( BigEndianWord[*] dataWA, int lenInBits)
'sha1' : function(){
// Note: all Section references below refer to FIPS 180-2.

// private utility functions

// - 32bit addition with wrap-around
var add_wa = function (x, y){
var lsw = (x & 0xFFFF) + (y & 0xFFFF);
var msw = (x >> 16) + (y >> 16) + (lsw >> 16);
return (msw << 16) | (lsw & 0xFFFF);
}

// - 32bit rotatate left
var rol = function(num, cnt) {
return (num << cnt) | (num >>> (32 - cnt));
}

// - round-dependent function f_t from Section 4.1.1
function sha1_ft(t, b, c, d) {
if(t < 20) return (b & c) | ((~b) & d);
if(t < 40) return b ^ c ^ d;
if(t < 60) return (b & c) | (b & d) | (c & d);
return b ^ c ^ d;
}

// - round-dependent SHA-1 constants from Section 4.2.1
function sha1_kt(t) {
return (t < 20) ?  1518500249 :
     (t < 40) ?  1859775393 :
     (t < 60) ? -1894007588 :
  /* (t < 80) */ -899497514 ;
}

// main algorithm. 
return function( /* BigEndianWord[*] */ dataWA, /* int */ lenInBits) {

// Section 6.1.1: Preprocessing
//-----------------------------
// 1. padding:  (see also Section 5.1.1)
//  - append one 1 followed by 0 bits filling up 448 bits of last (512bit) block
dataWA[lenInBits >> 5] |= 0x80 << (24 - lenInBits % 32);
//  - encode length in bits in last 64 bits
//    Note: we rely on javascript to zero file elements which are beyond last (partial) data-block
//    but before this length encoding!
dataWA[((lenInBits + 64 >> 9) << 4) + 15] = lenInBits;

// 2. 512bit blocks (actual split done ondemand later)
var W = Array(80);

// 3. initial hash using SHA-1 constants on page 13
var H0 =  1732584193;
var H1 = -271733879;
var H2 = -1732584194;
var H3 =  271733878;
var H4 = -1009589776;

// 6.1.2 SHA-1 Hash Computation
for(var i = 0; i < dataWA.length; i += 16) {
// 1. Message schedule, done below
// 2. init working variables
var a = H0; var b = H1; var c = H2; var d = H3; var e = H4;

// 3. round-functions
for(var j = 0; j < 80; j++)
{
		 // postponed step 2
  W[j] = ( (j < 16) ? dataWA[i+j] : rol(W[j-3] ^ W[j-8] ^ W[j-14] ^ W[j-16], 1));

  var T = add_wa( add_wa( rol(a, 5), sha1_ft(j, b, c, d)),
                  add_wa( add_wa(e, W[j]), sha1_kt(j)) );
  e = d;
  d = c;
  c = rol(b, 30);
  b = a;
  a = T;
}

 // 4. intermediate hash
H0 = add_wa(a, H0);
H1 = add_wa(b, H1);
H2 = add_wa(c, H2);
H3 = add_wa(d, H3);
H4 = add_wa(e, H4);
}

return Array(H0, H1, H2, H3, H4);
}
}()

};


////////////////////////////////////////////////////////////////////////////////
/*
http://www.JSON.org/json2.js
2008-11-19

Public Domain.

NO WARRANTY EXPRESSED OR IMPLIED. USE AT YOUR OWN RISK.

See http://www.JSON.org/js.html

This file creates a global JSON object containing two methods: stringify
and parse.

    JSON.stringify(value, replacer, space)
        value       any JavaScript value, usually an object or array.

        replacer    an optional parameter that determines how object
                    values are stringified for objects. It can be a
                    function or an array of strings.

        space       an optional parameter that specifies the indentation
                    of nested structures. If it is omitted, the text will
                    be packed without extra whitespace. If it is a number,
                    it will specify the number of spaces to indent at each
                    level. If it is a string (such as '\t' or '&nbsp;'),
                    it contains the characters used to indent at each level.

        This method produces a JSON text from a JavaScript value.

        When an object value is found, if the object contains a toJSON
        method, its toJSON method will be called and the result will be
        stringified. A toJSON method does not serialize: it returns the
        value represented by the name/value pair that should be serialized,
        or undefined if nothing should be serialized. The toJSON method
        will be passed the key associated with the value, and this will be
        bound to the object holding the key.

        For example, this would serialize Dates as ISO strings.

            Date.prototype.toJSON = function (key) {
                function f(n) {
                    // Format integers to have at least two digits.
                    return n < 10 ? '0' + n : n;
                }

                return this.getUTCFullYear()   + '-' +
                     f(this.getUTCMonth() + 1) + '-' +
                     f(this.getUTCDate())      + 'T' +
                     f(this.getUTCHours())     + ':' +
                     f(this.getUTCMinutes())   + ':' +
                     f(this.getUTCSeconds())   + 'Z';
            };

        You can provide an optional replacer method. It will be passed the
        key and value of each member, with this bound to the containing
        object. The value that is returned from your method will be
        serialized. If your method returns undefined, then the member will
        be excluded from the serialization.

        If the replacer parameter is an array of strings, then it will be
        used to select the members to be serialized. It filters the results
        such that only members with keys listed in the replacer array are
        stringified.

        Values that do not have JSON representations, such as undefined or
        functions, will not be serialized. Such values in objects will be
        dropped; in arrays they will be replaced with null. You can use
        a replacer function to replace those with JSON values.
        JSON.stringify(undefined) returns undefined.

        The optional space parameter produces a stringification of the
        value that is filled with line breaks and indentation to make it
        easier to read.

        If the space parameter is a non-empty string, then that string will
        be used for indentation. If the space parameter is a number, then
        the indentation will be that many spaces.

        Example:

        text = JSON.stringify(['e', {pluribus: 'unum'}]);
        // text is '["e",{"pluribus":"unum"}]'


        text = JSON.stringify(['e', {pluribus: 'unum'}], null, '\t');
        // text is '[\n\t"e",\n\t{\n\t\t"pluribus": "unum"\n\t}\n]'

        text = JSON.stringify([new Date()], function (key, value) {
            return this[key] instanceof Date ?
                'Date(' + this[key] + ')' : value;
        });
        // text is '["Date(---current time---)"]'


    JSON.parse(text, reviver)
        This method parses a JSON text to produce an object or array.
        It can throw a SyntaxError exception.

        The optional reviver parameter is a function that can filter and
        transform the results. It receives each of the keys and values,
        and its return value is used instead of the original value.
        If it returns what it received, then the structure is not modified.
        If it returns undefined then the member is deleted.

        Example:

        // Parse the text. Values that look like ISO date strings will
        // be converted to Date objects.

        myData = JSON.parse(text, function (key, value) {
            var a;
            if (typeof value === 'string') {
                a =
/^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2}(?:\.\d*)?)Z$/.exec(value);
                if (a) {
                    return new Date(Date.UTC(+a[1], +a[2] - 1, +a[3], +a[4],
                        +a[5], +a[6]));
                }
            }
            return value;
        });

        myData = JSON.parse('["Date(09/09/2001)"]', function (key, value) {
            var d;
            if (typeof value === 'string' &&
                    value.slice(0, 5) === 'Date(' &&
                    value.slice(-1) === ')') {
                d = new Date(value.slice(5, -1));
                if (d) {
                    return d;
                }
            }
            return value;
        });


This is a reference implementation. You are free to copy, modify, or
redistribute.

This code should be minified before deployment.
See http://javascript.crockford.com/jsmin.html

USE YOUR OWN COPY. IT IS EXTREMELY UNWISE TO LOAD CODE FROM SERVERS YOU DO
NOT CONTROL.
*/

/*jslint evil: true */

/*global JSON */

/*members "", "\b", "\t", "\n", "\f", "\r", "\"", JSON, "\\", apply,
call, charCodeAt, getUTCDate, getUTCFullYear, getUTCHours,
getUTCMinutes, getUTCMonth, getUTCSeconds, hasOwnProperty, join,
lastIndex, length, parse, prototype, push, replace, slice, stringify,
test, toJSON, toString, valueOf
*/

//Create a JSON object only if one does not already exist. We create the
//methods in a closure to avoid creating global variables.

if (!this.JSON) {
JSON = {};
}
(function () {

function f(n) {
    // Format integers to have at least two digits.
    return n < 10 ? '0' + n : n;
}

if (typeof Date.prototype.toJSON !== 'function') {

    Date.prototype.toJSON = function (key) {

        return this.getUTCFullYear()   + '-' +
             f(this.getUTCMonth() + 1) + '-' +
             f(this.getUTCDate())      + 'T' +
             f(this.getUTCHours())     + ':' +
             f(this.getUTCMinutes())   + ':' +
             f(this.getUTCSeconds())   + 'Z';
    };

    String.prototype.toJSON =
    Number.prototype.toJSON =
    Boolean.prototype.toJSON = function (key) {
        return this.valueOf();
    };
}

var cx = /[\u0000\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g,
    escapable = /[\\\"\x00-\x1f\x7f-\x9f\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g,
    gap,
    indent,
    meta = {    // table of character substitutions
        '\b': '\\b',
        '\t': '\\t',
        '\n': '\\n',
        '\f': '\\f',
        '\r': '\\r',
        '"' : '\\"',
        '\\': '\\\\'
    },
    rep;


function quote(string) {

//If the string contains no control characters, no quote characters, and no
//backslash characters, then we can safely slap some quotes around it.
//Otherwise we must also replace the offending characters with safe escape
//sequences.

    escapable.lastIndex = 0;
    return escapable.test(string) ?
        '"' + string.replace(escapable, function (a) {
            var c = meta[a];
            return typeof c === 'string' ? c :
                '\\u' + ('0000' + a.charCodeAt(0).toString(16)).slice(-4);
        }) + '"' :
        '"' + string + '"';
}


function str(key, holder) {

//Produce a string from holder[key].

    var i,          // The loop counter.
        k,          // The member key.
        v,          // The member value.
        length,
        mind = gap,
        partial,
        value = holder[key];

//If the value has a toJSON method, call it to obtain a replacement value.

    if (value && typeof value === 'object' &&
            typeof value.toJSON === 'function') {
        value = value.toJSON(key);
    }

//If we were called with a replacer function, then call the replacer to
//obtain a replacement value.

    if (typeof rep === 'function') {
        value = rep.call(holder, key, value);
    }

//What happens next depends on the value's type.

    switch (typeof value) {
    case 'string':
        return quote(value);

    case 'number':

//JSON numbers must be finite. Encode non-finite numbers as null.

        return isFinite(value) ? String(value) : 'null';

    case 'boolean':
    case 'null':

//If the value is a boolean or null, convert it to a string. Note:
//typeof null does not produce 'null'. The case is included here in
//the remote chance that this gets fixed someday.

        return String(value);

//If the type is 'object', we might be dealing with an object or an array or
//null.

    case 'object':

//Due to a specification blunder in ECMAScript, typeof null is 'object',
//so watch out for that case.

        if (!value) {
            return 'null';
        }

//Make an array to hold the partial results of stringifying this object value.

        gap += indent;
        partial = [];

//Is the value an array?

        if (Object.prototype.toString.apply(value) === '[object Array]') {

//The value is an array. Stringify every element. Use null as a placeholder
//for non-JSON values.

            length = value.length;
            for (i = 0; i < length; i += 1) {
                partial[i] = str(i, value) || 'null';
            }

//Join all of the elements together, separated with commas, and wrap them in
//brackets.

            v = partial.length === 0 ? '[]' :
                gap ? '[\n' + gap +
                        partial.join(',\n' + gap) + '\n' +
                            mind + ']' :
                      '[' + partial.join(',') + ']';
            gap = mind;
            return v;
        }

//If the replacer is an array, use it to select the members to be stringified.

        if (rep && typeof rep === 'object') {
            length = rep.length;
            for (i = 0; i < length; i += 1) {
                k = rep[i];
                if (typeof k === 'string') {
                    v = str(k, value);
                    if (v) {
                        partial.push(quote(k) + (gap ? ': ' : ':') + v);
                    }
                }
            }
        } else {

//Otherwise, iterate through all of the keys in the object.

            for (k in value) {
                if (Object.hasOwnProperty.call(value, k)) {
                    v = str(k, value);
                    if (v) {
                        partial.push(quote(k) + (gap ? ': ' : ':') + v);
                    }
                }
            }
        }

//Join all of the member texts together, separated with commas,
//and wrap them in braces.

        v = partial.length === 0 ? '{}' :
            gap ? '{\n' + gap + partial.join(',\n' + gap) + '\n' +
                    mind + '}' : '{' + partial.join(',') + '}';
        gap = mind;
        return v;
    }
}

//If the JSON object does not yet have a stringify method, give it one.

if (typeof JSON.stringify !== 'function') {
    JSON.stringify = function (value, replacer, space) {

//The stringify method takes a value and an optional replacer, and an optional
//space parameter, and returns a JSON text. The replacer can be a function
//that can replace values, or an array of strings that will select the keys.
//A default replacer method can be provided. Use of the space parameter can
//produce text that is more easily readable.

        var i;
        gap = '';
        indent = '';

//If the space parameter is a number, make an indent string containing that
//many spaces.

        if (typeof space === 'number') {
            for (i = 0; i < space; i += 1) {
                indent += ' ';
            }

//If the space parameter is a string, it will be used as the indent string.

        } else if (typeof space === 'string') {
            indent = space;
        }

//If there is a replacer, it must be a function or an array.
//Otherwise, throw an error.

        rep = replacer;
        if (replacer && typeof replacer !== 'function' &&
                (typeof replacer !== 'object' ||
                 typeof replacer.length !== 'number')) {
            throw new Error('JSON.stringify');
        }

//Make a fake root object containing our value under the key of ''.
//Return the result of stringifying the value.

        return str('', {'': value});
    };
}


//If the JSON object does not yet have a parse method, give it one.

if (typeof JSON.parse !== 'function') {
    JSON.parse = function (text, reviver) {

//The parse method takes a text and an optional reviver function, and returns
//a JavaScript value if the text is a valid JSON text.

        var j;

        function walk(holder, key) {

//The walk method is used to recursively walk the resulting structure so
//that modifications can be made.

            var k, v, value = holder[key];
            if (value && typeof value === 'object') {
                for (k in value) {
                    if (Object.hasOwnProperty.call(value, k)) {
                        v = walk(value, k);
                        if (v !== undefined) {
                            value[k] = v;
                        } else {
                            delete value[k];
                        }
                    }
                }
            }
            return reviver.call(holder, key, value);
        }


//Parsing happens in four stages. In the first stage, we replace certain
//Unicode characters with escape sequences. JavaScript handles many characters
//incorrectly, either silently deleting them, or treating them as line endings.

        cx.lastIndex = 0;
        if (cx.test(text)) {
            text = text.replace(cx, function (a) {
                return '\\u' +
                    ('0000' + a.charCodeAt(0).toString(16)).slice(-4);
            });
        }

//In the second stage, we run the text against regular expressions that look
//for non-JSON patterns. We are especially concerned with '()' and 'new'
//because they can cause invocation, and '=' because it can cause mutation.
//But just to be safe, we want to reject all unexpected forms.

//We split the second stage into 4 regexp operations in order to work around
//crippling inefficiencies in IE's and Safari's regexp engines. First we
//replace the JSON backslash pairs with '@' (a non-JSON character). Second, we
//replace all simple value tokens with ']' characters. Third, we delete all
//open brackets that follow a colon or comma or that begin the text. Finally,
//we look to see that the remaining characters are only whitespace or ']' or
//',' or ':' or '{' or '}'. If that is so, then the text is safe for eval.

        if (/^[\],:{}\s]*$/.
test(text.replace(/\\(?:["\\\/bfnrt]|u[0-9a-fA-F]{4})/g, '@').
replace(/"[^"\\\n\r]*"|true|false|null|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?/g, ']').
replace(/(?:^|:|,)(?:\s*\[)+/g, ''))) {

//In the third stage we use the eval function to compile the text into a
//JavaScript structure. The '{' operator is subject to a syntactic ambiguity
//in JavaScript: it can begin a block or an object literal. We wrap the text
//in parens to eliminate the ambiguity.

            j = eval('(' + text + ')');

//In the optional fourth stage, we recursively walk the new structure, passing
//each name/value pair to a reviver function for possible transformation.

            return typeof reviver === 'function' ?
                walk({'': j}, '') : j;
        }

//If the text is not JSON parseable, then a SyntaxError is thrown.

        throw new SyntaxError('JSON.parse');
    };
}
})();

////////////////////////////////////////////////////////////////////////////////

if(!window["PageBus"])
	window.PageBus = {};
 
// Insert a debugger breakpoint in Dev builds of PageBus only. The debugger line should be removed in production builds.
PageBus._debug = function() {
	// debugger; // REMOVE ON BUILD
};

PageBus._esc = function(s) {
	return s.replace(/\./g,"!");
};

PageBus._assertPubTopic = function(topic) {
    if ((topic == null) || (topic == "") || (topic.indexOf("*") != -1) || (topic.indexOf("..") != -1) || 
        (topic.charAt(0) == ".") || (topic.charAt(topic.length-1) == "."))
    {
        throw new Error(OpenAjax.hub.Error.BadParameters);
    }
};

PageBus._assertSubTopic = function(topic) {
	if((topic == null) || (topic == ""))
		throw new Error(OpenAjax.hub.Error.BadParameters);
    var path = topic.split(".");
    var len = path.length;
    for (var i = 0; i < len; i++) {
        var p = path[i];
        if ((p == "") ||
           ((p.indexOf("*") != -1) && (p != "*") && (p != "**"))) {
            throw new Error(OpenAjax.hub.Error.BadParameters);
        }
        if ((p == "**") && (i < len - 1)) {
            throw new Error(OpenAjax.hub.Error.BadParameters);
        }
    }
};

PageBus._copy = function(obj) {
	var c;
	if( typeof(obj) == "object" ) {
		if(obj == null)
			return null;
		else if(obj.constructor == Array) {
			c = [];
			for(var i = 0; i < obj.length; i++)
				c[i] = PageBus._copy(obj[i]);
			return c;
		}
		else if(obj.constructor == Date) {
			c = new Date();
			c.setDate(obj.getDate());
			return c;
		}
		c = {};
		for(var p in obj) 
			c[p] = PageBus._copy(obj[p]);
		return c;
	}
	else {
		return obj;
	}
};

PageBus._TopicMatcher = function() {
	this._items = {};
};

PageBus._TopicMatcher.prototype.store = function( topic, val ) {
    var path = topic.split(".");
    var len = path.length;
    _recurse = function(tree, index) {
        if (index == len)
    		tree["."] = { topic: topic, value: val };
        else { 
            var token = path[index];
            if (!tree[token])
                tree[token] = {}; 
            _recurse(tree[token], index + 1);
        }
    };
    _recurse( this._items, 0 );
};

PageBus._TopicMatcher.prototype.match = function( topic, exactMatch ) {
    var path = topic.split(".");
    var len = path.length;
	var res = [];
    _recurse = function(tree, index) {
    	if(!tree)
    		return;
    	var node;
        if (index == len)
            node = tree;
        else {	
            _recurse(tree[path[index]], index + 1);
            if(exactMatch)
            	return;
            if(path[index] != "**") 
            	_recurse(tree["*"], index + 1);
            node = tree["**"];
        }
        if ( (!node) || (!node["."]) )
        	return;
        res.push(node["."]);
    };
    _recurse( this._items, 0 );
    return res;
};

PageBus._TopicMatcher.prototype.exists = function( topic, exactMatch ) {
    var path = topic.split(".");
    var len = path.length;
	var res = false;
    _recurse = function(tree, index) {
    	if(!tree)
    		return;
    	var node;
        if (index == len)
            node = tree;
        else {	
            _recurse(tree[path[index]], index + 1);
            if(res || exactMatch)
            	return;
            if(path[index] != "**") {
            	_recurse(tree["*"], index + 1);
	            if(res)
	            	return;
            }
            node = tree["**"];
        }
        if ( (!node) || (!node["."]) )
        	return;
        res = true;
    };
    _recurse( this._items, 0 );
    return res;
};

PageBus._TopicMatcher.prototype.clear = function( topic ) {
    var path = topic.split(".");
    var len = path.length;
    _recurse = function(tree, index) {
    	if(!tree)
    		return;
        if (index == len) {
            if (tree["."])
            	delete tree["."];
        }
        else {	
        	_recurse(tree[path[index]], index + 1);
            for(var x in tree[path[index]]) {
            	return;
            }
        	delete tree[path[index]];
        }
    };
    _recurse( this._items, 0 );
};

PageBus._TopicMatcher.prototype.wildcardClear = function( topic ) {
    var path = topic.split(".");
    var len = path.length;    
    _clean = function(node, tok) {
		for(m in node[tok])
			return;
		delete node[tok];
    };
    _recurse = function(tree, index) {
    	if(!tree)
    		return;
    	
        if (index == len) {	
        	if (tree["."])
        		delete tree["."];
        	return;
        }
        else {	
        	var tok = path[index];
        	var n;        	
        	if(tree[tok]) {	
        		_recurse(tree[tok], index + 1);
        		_clean(tree, tok);
	        }
        	if(tok == "*") {
            	for(n in tree) {
            		if(( n != "**" ) && (n != ".") ) {
            			_recurse(tree[n], index + 1);
            			_clean(tree, n);
            		}
            	}
            } 
            else if(tok == "**") {
            	for(n in tree) {
            		delete tree[n];
            	}
            }
        }
        return;
    };
    _recurse( this._items, 0 );
};

PageBus._TopicMatcher.prototype.wildcardMatch = function( topic ) {
    var path = topic.split(".");
    var len = path.length;
    var res = [];
	_recurse = function( tree, index ) {
		var tok = path[index];
		var node;
		if( (!tree) || (index == len) )
			return;		
		if( tok == "**" ) {
			for( var n in tree ) {
				if( n != "." ) {
					node = tree[n];
					if( node["."] )
						res.push( node["."] );
					_recurse( node, index );
				}
			}
		}
		else if( tok == "*" ) {
			for( var n in tree ) {
				if( (n != ".") && (n != "**") ){
					node = tree[n];
					if( index == len - 1 ) {
						if( node["."] )			
							res.push( node["."] );
					}
					else
						_recurse( node, index + 1 );
				}
			}
		} 
		else {
			node = tree[tok];
			if(!node)
				return;
			if( index == len - 1 ) {
				if( node["."] )
					res.push( node["."] );
			}
			else 
				_recurse( node, index + 1 );
		}
	};
    _recurse( this._items, 0 );
    return res;
};


////////////////////////////////////////////////////////////////////////////////////


PageBus.policy = {
	Ops: {
		Publish: "p",
		Subscribe: "s"
	},
	Error: {
		BadParameters: "PageBus.policy.Error.BadParameters"
	},
	_assertName: function(topic) {
		if((topic == null) || (topic == ""))
			throw new Error(OpenAjax.hub.Error.BadParameters);
		if(PageBus.policy._tops[topic])
			return;
	    var path = topic.split(".");
	    var len = path.length;
	    for (var i = 0; i < len; i++) {
	        var p = path[i];
	        if ((p == "") ||
	           ((p.indexOf("*") != -1) && (p != "*") && (p != "**"))) {
	            throw new Error(PageBus.policy.Error.BadParameters);
	        }
	        if ((p == "**") && (i < len - 1)) {
	            throw new Error(PageBus.policy.Error.BadParameters);
	        }
	    }
	    PageBus.policy._tops[topic] = true;
	},
	_tops: {}
};

PageBus.policy.HubPolicy = function( params ) {
	if(!params)
		params = {};
	this._cfg = params;
	this._log = params["log"];
	this._topicMgr = new PageBus._TopicMatcher();	
};

PageBus.policy.HubPolicy.prototype.onPublish = function( topic, data, pc, sc ) {
	var res = true;
	var origin;
	if(sc != null) {
		origin = sc.getPartnerOrigin();
		if(!origin)
			return false;
		res = this.isAllowed.call(this, origin, PageBus.policy.Ops.Subscribe, topic);
	}
	if( res && (pc != null) ) {
		origin = pc.getPartnerOrigin();
		if(!origin)
			return false;
		res = this.isAllowed.call(this, origin, PageBus.policy.Ops.Publish, topic);
	}
	if(this._log) {
		var sid = sc ? sc.getClientID() : "(Mgr)";
		var pid = pc ? pc.getClientID() : "(Mgr)";
		this._log( "(PageBus.policy) [" + pid + ", " + sid + "] onPublish: " + 
				(res ? "ALLOWED " : "DENIED  ") + topic );		
	}
	return res;
};

PageBus.policy.HubPolicy.prototype.onSubscribe = function( topic, sc ) {
	var res = true;
	var origin;
	if(sc != null) {
		origin = sc.getPartnerOrigin();
		if(!origin)
			return false;
		res = this.isAllowed.call(this, origin, PageBus.policy.Ops.Subscribe, topic);
	}
	if(this._log) {
		var cid = sc ? sc.getClientID() : "(Mgr)";
		this._log("(PageBus.policy) [" + cid + "] onSubscribe: " + 
				(res ? "ALLOWED " : "DENIED  ") + topic );
	}
	return res;
};

PageBus.policy.HubPolicy.prototype.onUnsubscribe = function( topic, sc ) { };

PageBus.policy.HubPolicy.prototype.onSend = function( topic, data, origin) {
	var res = this.isAllowed(origin, PageBus.policy.Ops.Subscribe, topic);
	if(this._log)
		this._log("(PageBus.policy) [" + origin + "] onSend: " + (res ? "ALLOWED " : "DENIED  ") + topic);
	return res;
};

PageBus.policy.HubPolicy.prototype.onReceive = function( topic, data, origin) {
	var res = this.isAllowed(origin, PageBus.policy.Ops.Publish, topic);
	if(this._log)
		this._log("(PageBus.policy) [" + origin + "] onReceive: " + (res ? "ALLOWED " : "DENIED  ") + topic);
	return res;
};

PageBus.policy.HubPolicy.prototype._getMyOrigin = function() {
	var o = window.location.href.match(/[^:]*:\/\/[^:\/\?#]*/);
	return o[0];
};

PageBus.policy.HubPolicy.prototype.grant = function( origin, op, name ) {
	if( (!origin) || (!op) || (!name) )
		throw new Error(PageBus.policy.Error.BadParameters);
	var t = PageBus._esc(origin) + "." + op + "." + name;
	PageBus.policy._assertName(t);
	this._topicMgr.store(t, { dm: origin, op: op, tp: name });	
	if(this._log)
		this._log("(PageBus.policy) [" + origin + "] grant: " + op + " on " + name);
	var cacheName = "_pagebus.cache.s." + name;
	this._topicMgr.store(PageBus._esc(origin) + "." + op + "." + cacheName, { dm: origin, op: op, tp: cacheName });
	if(this._log)
		this._log("(PageBus.policy) [" + origin + "] implicit grant: " + op + " on " + cacheName);
};

PageBus.policy.HubPolicy.prototype.revoke = function( origin, op, name ) {
	if( (!origin) || (!op) || (!name) )
		throw new Error(PageBus.policy.Error.BadParameters);
	var t = PageBus._esc(origin) + "." + op + "." + name;
	PageBus.policy._assertName(t);
	this._topicMgr.clear(t);
	if(this._log)
		this._log("(PageBus.policy) [" + origin + "] revoke: " + op + " on " + name);
	var cacheName = "_pagebus.cache.s." + name;
	this._topicMgr.clear(PageBus._esc(origin) + "." + op + "." + cacheName);
	if(this._log)
		this._log("(PageBus.policy) [" + origin + "] implicit revoke: " + op + " on " + cacheName);
};

PageBus.policy.HubPolicy.prototype.revokeAll = function( origin ) {
	if( (!origin) )
		throw new Error(PageBus.policy.Error.BadParameters);
	this._topicMgr.wildcardClear(PageBus._esc(origin) + ".**");
	if(this._log)
		this._log("(PageBus.policy) [" + origin + "] revokeAll");
};

PageBus.policy.HubPolicy.prototype.isAllowed = function( origin, op, name ) {
	if((!origin) || (origin == "") || (!op) || (op == "") || (!name) || (name == "") )
		throw new Error(PageBus.policy.Error.BadParameters);
	var t = PageBus._esc(origin) + "." + op + "." + name;
	return this._topicMgr.exists(t, false);
};

PageBus.policy.HubPolicy.prototype.listAllowed = function( origin, op ) {
	if( (!origin) || (!op) )
		throw new Error(PageBus.policy.Error.BadParameters);
	var qr = this._topicMgr.wildcardMatch(PageBus._esc(origin) + "." + op + ".**");
	var res = [];
	for(var r in qr) {
		if(qr[r].value.tp.substring(0,9) != "_pagebus.")
			res.push(qr[r].value.tp);
	}
	return res;	
};


PageBus.cache = {};

PageBus.cache.Error = {
	// This topic is not being cached by the local hub instance
	NoCache: "PageBus.cache.Error.NoCache"	
};

PageBus._cache = {};

PageBus._cache.isCacheable = function( subData ) {
	return ( (subData) && (typeof subData == "object") && (subData["PageBus"]) && (subData.PageBus["cache"]) );
};

PageBus._cache.Cache = function() {
	this._refs = {};
	this._doCache = new PageBus._TopicMatcher();
	this._caches = new PageBus._TopicMatcher();
};

PageBus._cache.Cache.prototype.add = function( topic, subID ) {
	var dc;
	var dca = this._doCache.match(topic, true);
	if(dca.length > 0) 
		dc = dca[0].value;
	else {
		dc = { rc: 0 };
		this._doCache.store(topic, dc);
	}
	dc.rc++;
	this._refs[subID] = topic;
};

PageBus._cache.Cache.prototype.remove = function( subID ) {
	var topic = this._refs[subID];
	if(!topic)
		return;
	delete this._refs[subID];
	var dca = this._doCache.match(topic, true);
	if(dca.length == 0) 
		return;	
	dca[0].value.rc--;
	if(dca[0].value.rc == 0) {			
		this._doCache.clear(topic);
		var caches = this._caches.wildcardMatch(topic);
		for(var i = 0; i < caches.length; i++) {
			if( !(this._doCache.exists(caches[i].topic, false)) )
				this._caches.clear(caches[i].topic);
		}
	}
};

PageBus._cache.Cache.prototype.storeCopy = function( topic, value ) {
	PageBus._assertPubTopic(topic);
	var copy = PageBus._copy(value);
	this._caches.store(topic, copy);
};

PageBus._cache.Cache.prototype.clear = function( topic, value ) {
	PageBus._assertPubTopic(topic);
	this._caches.clear(topic);
};

PageBus._cache.Cache.prototype.query = function( topic ) {
	PageBus._assertSubTopic(topic); 
	return this._caches.wildcardMatch(topic);
};

PageBus._cache.Cache.prototype.isCaching = function( topic ) {
	for (var r in this._refs)
		false;
	return this._doCache.exists(topic, false);
};


PageBus._enableMH = function() {
	var MHClass = OpenAjax.hub.ManagedHub;	
	OpenAjax.hub.ManagedHub = function( params ) {
		if(!params) {
			throw new Error(OpenAjax.hub.Error.BadParameters);
		}
		if( (!params.onPublish) || (!params.onSubscribe) ) {
			throw new Error(OpenAjax.hub.Error.BadParameters);
		}		
		var defaultParams = {};
		var defaultPolicy = null;
		if(!params["PageBus"]) 
			params.PageBus = {};
		if(!params.PageBus["policy"]) 
			params.PageBus.policy = defaultPolicy;
		if(!params["scope"])
			params.scope = window;		
		this._pagebus = {
			_params: {},
			_getActualParameters: function() { return this._pagebus._params; },
			_policy: params.PageBus.policy,
			_hub: this
		};

		// newly added 
		this.pagebus = { _hub: this };
		
		var pb = this._pagebus;
		pb._cache = new PageBus._cache.Cache();
		this.pagebus.query = function( topic ) {
			return this._hub._pagebus._cache.query( topic );
		};
		this.pagebus.store = function( topic, data ) {
			if(this._hub._pagebus._cache.isCaching(topic)) 
				this._hub.publish( topic, data );
		};
		this.pagebus.clear = function( topic ) {
			if(this._hub._pagebus._cache.isCaching(topic)) 
				this._hub.publish( topic, null );
		};
		var appScope = params.scope ? params.scope : window;
		var params2 = this._pagebus._params;
		for(var pn in params) {
			params2[pn] = params[pn];
		}
		params2.scope = params.scope ? params.scope : window;
		params2.onPublish = function(topic, data, pcont, scont) {
			try {
				var pol = params2.PageBus.policy;
				if(pol) {
					var res = pol.onPublish.call(pol, topic, data, pcont, scont);
					if(!res)
						return false;
				}
				return params.onPublish.call(appScope, topic, data, pcont, scont);
			}
			catch(e) {
				return false;
			}
		};
		params2.onSubscribe = function(topic, scont) {
			try {
				var pol = params2.PageBus.policy;
				if(pol) {
					var res = pol.onSubscribe.call(pol, topic, scont);
					if(!res)
						return false;
				}
				res = params.onSubscribe.call(appScope, topic, scont);
				return res;
			}
			catch(e) {
				return false;
			}
		};
		params2.onUnsubscribe = function(topic, scont) {
			try {
				var pol = params2.PageBus.policy;
				if(pol) {
					pol.onUnsubscribe.call(pol, params.scope, topic, scont);
				}
				params.onUnsubscribe.call(appScope, topic, scont);
			}
			catch(e) {
				return;
			}
		};
		if(!pb._policy) {			
			pb._policy = null; // new PageBus.policy.HubPolicy(params);
		}		
		MHClass.call( this, params2 );
		this.getParameters = function() { return params; };
	};
	OpenAjax.hub.ManagedHub.prototype = MHClass.prototype;	
	var p = OpenAjax.hub.ManagedHub.prototype.publish;
	var s = OpenAjax.hub.ManagedHub.prototype.subscribe;
	var u = OpenAjax.hub.ManagedHub.prototype.unsubscribe;
	var p4c = OpenAjax.hub.ManagedHub.prototype.publishForClient;
	var s4c = OpenAjax.hub.ManagedHub.prototype.subscribeForClient;
	var u4c = OpenAjax.hub.ManagedHub.prototype.unsubscribeForClient;
	var gs = OpenAjax.hub.ManagedHub.prototype.getScope;	
	OpenAjax.hub.ManagedHub.prototype.publish = function( topic, data ) {
		PageBus._assertPubTopic(topic);
		if(this._pagebus._cache.isCaching(topic)) {
			if(data == null)
				this._pagebus._cache.clear(topic);
			else
				this._pagebus._cache.storeCopy(topic, data);
		}
		p.call( this, topic, data );
	};
	OpenAjax.hub.ManagedHub.prototype.subscribe = function( topic, onData, scope, onComplete, subscriberData ) {
		PageBus._assertSubTopic(topic);
		var sid = s.call( this, topic, onData, scope, onComplete, subscriberData );
		if(PageBus._cache.isCacheable(subscriberData)) {
			var cache = this._pagebus._cache;
			cache.add(topic, sid);
			var vals = cache.query(topic);
			for (var i = 0; i < vals.length; i++) {
				try {
					onData.call(scope ? scope : window, vals[i].topic, vals[i].value, subscriberData);
				}
				catch(e) {
					PageBus._debug();
				}
			}
		}
		return sid;
	};
	OpenAjax.hub.ManagedHub.prototype.unsubscribe = function( subID, onComplete, scope ) {
		var cache = this._pagebus._cache;
		cache.remove(subID); 
		u.call( this, subID, onComplete, scope );
	};
	OpenAjax.hub.ManagedHub.prototype.getScope = function() {
		return gs.call( this );
	};	
	OpenAjax.hub.ManagedHub.prototype.publishForClient = function( container, topic, data ) {
		PageBus._assertPubTopic(topic);
		if( (!this._pagebus._policy) || 
		    ( this._pagebus._policy.isAllowed(container.getPartnerOrigin(), PageBus.policy.Ops.Publish, topic) ) ) {
			if(this._pagebus._cache.isCaching(topic)) { 
				if(data == null)
					this._pagebus._cache.clear(topic);
				else
					this._pagebus._cache.storeCopy(topic, data);
			}
		}
		p4c.call( this, container, topic, data );
	};
	OpenAjax.hub.ManagedHub.prototype.subscribeForClient = function( container, topic, containerSubID ) {
		PageBus._assertSubTopic(topic);
		var mgrSubID = s4c.call( this, container, topic, containerSubID ); 
		if(topic.substring(0, 17) == "_pagebus.cache.s.") {
			var t = topic.substring(17);
			this._pagebus._cache.add(t, mgrSubID);
			var vals = this._pagebus._cache.query(t);
			
			function _sendValues( ) {
				for(var i = 0; i < vals.length; i++) {
					container.sendToClient(vals[i].topic, vals[i].value, containerSubID);
				}
			}
			setTimeout( _sendValues, 0 );
		}
		return mgrSubID;
	};
	OpenAjax.hub.ManagedHub.prototype.unsubscribeForClient = function( container, managerSubID ) {
		this._pagebus._cache.remove(managerSubID); 
		try {
			var sdata = this.getSubscriberData(managerSubID);
			if(PageBus._cache.isCacheable(sdata)) {
				if(this._pagebus._cacheSids[managerSubID]) { 
					this.unsubscribe(this._pagebus._cacheSids[managerSubID], null, null);
					delete this._pagebus._cacheSids[managerSubID];
					this._pagebus._cache.remove(managerSubID);		
				}
			}
			u4c.call( this, container, managerSubID );
		}
		catch(e) {
			PageBus._debug();
		}
	};
};
PageBus._enableMH();

PageBus.HubClientExtender = function( hub, params ) {
	var that = this;
	this._hub = hub;
	
	// Set up default parameters
	
	this._params = params;
	if(!params["PageBus"])
		this._params.PageBus = { log: params.log };
	if(!params.PageBus["policy"]) {
		params.PageBus.policy = null;
	}
	
	// Initialize this HubClientExtender
	
	this._policy = params.PageBus.policy;
	this._cache = new PageBus._cache.Cache();
	this._cacheSids = {};
	
	// Store references to the hub's sub, unsub and publish functions:
	
	this._wrappedSubscribe = hub.subscribe;
	this._wrappedUnsubscribe = hub.unsubscribe;
	this._wrappedPublish = hub.publish;
	this._wrappedDisconnect = hub.disconnect;
	
	// Replace the hub's pub, sub and uns functions with wrappers:
	
	hub.publish = function( topic, data ) {
		that._publishWrapper( topic, data );
	};
	
	hub.subscribe = function( topic, onData, scope, onComplete, subData ) {
		return that._subscribeWrapper( topic, onData, scope, onComplete, subData );
	};
	
	hub.unsubscribe = function( subscriptionID, onComplete, scope ) {
		that._unsubscribeWrapper( subscriptionID, onComplete, scope );
	};
	
	hub.disconnect = function( onComplete, scope ) {
		that._disconnectWrapper( onComplete, scope );
	}
};

PageBus.HubClientExtender.prototype._publishWrapper = function( topic, data ) {

	if(!this._hub.isConnected())
		throw new Error(OpenAjax.hub.Error.Disconnected);

	PageBus._assertPubTopic(topic);
	if(this._cache.isCaching(topic)) {
		if(data == null)
			this._cache.clear(topic);
		else
			this._cache.storeCopy(topic, data);
	}
	
	var origin = this._hub.getPartnerOrigin();
	if(!origin)
		throw new Error(OpenAjax.hub.Error.Disconnected);
	if( this._policy && (! this._policy.onSend( topic, data, origin )) )
		return;
	this._wrappedPublish.call( this._hub, topic, data );
};

PageBus.HubClientExtender.prototype._subscribeWrapper = function( topic, onData, scope, onComplete, subData ) {	
	var that = this;
	if(!this._hub.isConnected())
		throw new Error(OpenAjax.hub.Error.Disconnected);
	PageBus._assertSubTopic(topic);
	if(!onData)
		throw new Error(OpenAjax.hub.Error.BadParameters);

	var policy = this._policy;
	var origin = this._hub.getPartnerOrigin();
	if(!origin)
		throw new Error(OpenAjax.hub.Error.Disconnected);
	if(policy && (! policy.onReceive.call( policy, topic, null, origin )) ) 
		throw new Error(OpenAjax.hub.Error.NotAllowed);
	
	dataHook = function( t, d, sd ) {			
		var policy = that._policy;
		var origin = that._hub.getPartnerOrigin();
		if(policy && (! policy.onReceive.call( policy, t, d, origin )) ) 
			return;
		if(PageBus._cache.isCacheable(sd)) {
			if(that._cache.isCaching(t)) {
				if(d == null)
					that._cache.clear(t);
				else
					that._cache.storeCopy(t, d);
			}
		}				
		try {
			var s = scope ? scope : window;
			onData.call(s, t, d, sd);
		}
		catch(e) {
			PageBus._debug();
		}
	};
	completeHook = function( item, suc, err ) {
		if(!suc) {			
			if(that._cacheSids[item]) { 
				that._hub.unsubscribe(that._cacheSids[item], null, null);
				delete that._cacheSids[item];
				that._cache.remove(item);		
			}
		}
		
  		try {
			var s = scope ? scope : window;
			onComplete.call(s, item, suc, err);
		}
		catch(e) {
			PageBus._debug();
		}
	};
	
	var sid = this._wrappedSubscribe.call( this._hub, topic, dataHook, scope, completeHook, subData );		
	
	try {
		this._hub.getSubscriberData( sid );
	} catch(e) {
		if( e.message == OpenAjax.hub.Error.NoSubscription ) {
			// unsubscribe was synchronously called within completeHook
			return sid;
		}
	}
	
	if( PageBus._cache.isCacheable(subData) ) {
		this._cache.add( topic, sid );
		this._cacheSids[sid] = this._hub.subscribe(
			"_pagebus.cache.s." + topic, 
			function(t,d,sd) { 
				var policy = this._policy;
				var origin = this._hub.getPartnerOrigin();
				if(policy && (! this._policy.onReceive( t, d, origin )) ) 
					return;
				if(PageBus._cache.isCacheable(subData)) {
					if(this._cache.isCaching(t)) {
						if(d == null)
							this._cache.clear(t);
						else
							this._cache.storeCopy(t, d);
					}
				}
				try {
					var s = scope ? scope : window;
					onData.call( s, t, d, subData );
				}
				catch(e) {
					PageBus._debug();
				}
			}, 
			this, 
			function(item, suc, err) {}, 
			null);
	}
	return sid;
};

PageBus.HubClientExtender.prototype._unsubscribeWrapper = function( subscriptionID, onComplete, scope ) {
	if(!this._hub.isConnected())
		throw new Error(OpenAjax.hub.Error.Disconnected);
	if( (subscriptionID == null) || (subscriptionID == "") )
		throw new Error(OpenAjax.hub.Error.BadParameters);
	var sdata = this._hub.getSubscriberData(subscriptionID);
	if(PageBus._cache.isCacheable(sdata)) {
		if(this._cacheSids[subscriptionID]) { 
			this._hub.unsubscribe(this._cacheSids[subscriptionID], null, null);
			delete this._cacheSids[subscriptionID];
			this._cache.remove(subscriptionID);		
		}
	}
	this._wrappedUnsubscribe.call( this._hub, subscriptionID, onComplete, scope );
};

PageBus.HubClientExtender.prototype._disconnectWrapper = function( onComplete, scope ) {
	this._cache._caches.wildcardClear("**");
	this._cacheSids = {};	
	this._wrappedDisconnect.call( this._hub, onComplete, scope );
};

PageBus.HubClientExtender.prototype.query = function( topic ) {
	return this._cache.query( topic ); // do not throw NoCache; can use query across broad ranges that are partly cached.
};

PageBus.HubClientExtender.prototype.store = function( topic, data ) {
	if(this._cache.isCaching(topic)) 
		this._hub.publish( topic, data );
	else
		throw new Error( PageBus.cache.Error.NoCache );
};

PageBus.HubClientExtender.prototype.clear = function( topic ) {
	if(this._cache.isCaching(topic)) 
		this._hub.publish( topic, null );
	else
		throw new Error( PageBus.cache.Error.NoCache );
};


/**
 * enableHubClientClass
 * Global function that prepares a HubClient class so that when it is 
 * instantiated via the "new" operator, the resulting instance is 
 * PageBus-enabled.
 */
PageBus.enableHubClientClass = function( hubClass ) {	
	hubClass.prototype._pagebusWrappedConnect = hubClass.prototype.connect;
	
	hubClass.prototype.connect = function( onComplete, scope ) {
		var hub = this;
		if( !hub.pagebus ) {
			hub.pagebus = new PageBus.HubClientExtender( hub, hub.getParameters() );
		}
		hub._pagebusWrappedConnect.call( hub, onComplete, scope );
	};

};


//////////////////////////////////////////////////////////////////////

//PageBus-enable the reference implementation:

if(!OpenAjax.hub) {
	debugger;
}

if(OpenAjax.hub.InlineHubClient)
	PageBus.enableHubClientClass(OpenAjax.hub.InlineHubClient);
if(OpenAjax.hub.IframeHubClient)
	PageBus.enableHubClientClass(OpenAjax.hub.IframeHubClient);

OpenAjax.hub._hub = new OpenAjax.hub.ManagedHub({ 
    onSubscribe: function(topic, ctnr) { return true; },
    onPublish: function(topic, data, pcont, scont) { return true; }
});

OpenAjax.hub.subscribe = function(topic, onData, scope, subscriberData) {
    if ( typeof onData === "string" ) {
        scope = scope || window;
        onData = scope[ onData ] || null;
    }    
    return OpenAjax.hub._hub.subscribe( topic, onData, scope, null, subscriberData );
}

OpenAjax.hub.unsubscribe = function(subscriptionID) {
    return OpenAjax.hub._hub.unsubscribe( subscriptionID );
}

OpenAjax.hub.publish = function(topic, data) {
    OpenAjax.hub._hub.publish(topic, data);
}

//////////////////////////////////////////////////////////////////////

PageBus.publish = function(topic, data) {
	OpenAjax.hub.publish(topic, data);
};
PageBus.subscribe = function(topic, scope, onData, subscriberData) {
	return OpenAjax.hub.subscribe(topic, onData, scope, subscriberData);
};
PageBus.unsubscribe = function(sub) {
	OpenAjax.hub.unsubscribe(sub);
};
PageBus.store = function(topic, data) {
	OpenAjax.hub._hub.pagebus.store(topic, data);
};
PageBus.query = function(topic) {
	return OpenAjax.hub._hub.pagebus.query(topic);
};

OpenAjax.hub.registerLibrary("PageBus", "http://pagebus.org/pagebus", "2.0", {});

