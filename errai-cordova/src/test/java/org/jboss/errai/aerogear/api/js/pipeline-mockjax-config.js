(function( $ ) {
// Use mockjax to intercept the rest calls and return data to the tests
// Clean up any mocks from previous tests first
$.mockjaxClear();

// read mock
$.mockjax({
    url: "tasks",
    type: "GET",
    headers: {
        "Content-Type": "application/json"
    },
    responseText: [
        {
            "^EncodedType":"org.jboss.errai.aerogear.api.pipeline.impl.PipeTest$Task",
            "^ObjectID":"1",
            id: 12345,
            title: "Do Something",
            date: "2012-08-01"
        },
        {
            "^EncodedType":"org.jboss.errai.aerogear.api.pipeline.impl.PipeTest$Task",
            "^ObjectID":"1",
            id: 67890,
            title: "Do Something Else",
            date: "2012-08-02"
        }
    ]
});

// save mock
$.mockjax({
    url: "tasks/*",
    type: "PUT",
    headers: {
        "Content-Type": "application/json"
    },
    responseText: {
        "^EncodedType":"org.jboss.errai.aerogear.api.pipeline.impl.PipeTest$Task",
        "^ObjectID":"1",
        id: 11223,
        title: "Updated Task",
        date: "2012-08-01"
    }
});

// delete mock
$.mockjax({
    url: "tasks/123",
    type: "DELETE",
    headers: {
        "Content-Type": "application/json"
    },
    responseText: []
});

})( jQuery );
