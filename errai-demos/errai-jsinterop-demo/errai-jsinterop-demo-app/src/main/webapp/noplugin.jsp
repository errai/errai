<!DOCTYPE html>
<% String contextPath = getServletContext().getContextPath(); %>
<html lang="en">
<head>
<meta charset="utf-8">
<title>Errai JS Interop Demo</title>
<meta name="description" content="">
<meta name="author" content="">

<!-- Le HTML5 shim, for IE6-8 support of HTML elements -->
<!--[if lt IE 9]>
      <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->

<!-- Le styles -->
<link href="<%= contextPath %>/bootstrap/css/bootstrap.css" rel="stylesheet">
<link href="<%= contextPath %>/css/application.css" rel="stylesheet">
<link href="<%= contextPath %>/bootstrap/css/bootstrap-responsive.css" rel="stylesheet">

<!-- Le fav and touch icons -->
<link rel="shortcut icon" href="<%= contextPath %>/favicon.ico">
<link rel="apple-touch-icon" href="<%= contextPath %>/favicon.ico">
    
    <script type="text/javascript" language="javascript" src="<%= contextPath %>/app/app.nocache.js"></script>
    <script type="text/javascript">
        var erraiBusRemoteCommunicationEnabled = false;
        var erraiSimpleFormatString = "%1$tc %2$s%n%4$s: %5$s%6$s%n";
        // This parameter is only used if Push State is enabled.
        var erraiApplicationWebContext="<%= contextPath %>";
		//	Comment the line below to enable Errai PushState
        // var erraiPushStateEnabled = true;
    </script>
</head>

<body>
	<iframe src="javascript:''" id="__gwt_historyFrame"
		style="width: 0; height: 0; border: 0"></iframe>
  <div class="navbar navbar-fixed-top">
      <div class="navbar-inner">
          <div class="container">
              <a class="brand" href="#">Errai</a>
          </div>
      </div>
  </div>
  <div id="rootPanel" class="container">
    <div class=row>
      <div data-field=welcome class="span10 hero-unit">
        <h1>Errai Ipsum Generator</h1>
        <br>
        <p>This demo is a show-cases Errai's JS interop support. The main application displays
        lorem-ipsum-like generators to users, and allows generation of filler text from any of
        the generators.
        <p>And Errai bean implementing the <b>IpsumGenerator</b> interface is discovered
        automatically &mdash; even from a separately compiled script!
        <p>Below you can try generating text from the two generators. The <b>Lorem Ipsum</b>
        generator was compiled as part of the main application. But the <b>Hipster Ipsum</b>
        generator is compiled separately in the errai-jsinterop-demo-plugin module.
      </div>
    </div>
  </div>

	<!-- Le javascript
    ================================================== -->
	<!-- Placed at the end of the document so the pages load faster -->
	<script src="<%= contextPath %>/js/jquery.min.js"></script>
	<script src="<%= contextPath %>/bootstrap/js/bootstrap-transition.js"></script>
	<script src="<%= contextPath %>/bootstrap/js/bootstrap-alert.js"></script>

	<script src="<%= contextPath %>/bootstrap/js/bootstrap-modal.js"></script>
	<script src="<%= contextPath %>/bootstrap/js/bootstrap-dropdown.js"></script>
	<script src="<%= contextPath %>/bootstrap/js/bootstrap-scrollspy.js"></script>
	<script src="<%= contextPath %>/bootstrap/js/bootstrap-tab.js"></script>
	<script src="<%= contextPath %>/bootstrap/js/bootstrap-tooltip.js"></script>
	<script src="<%= contextPath %>/bootstrap/js/bootstrap-popover.js"></script>

	<script src="<%= contextPath %>/bootstrap/js/bootstrap-button.js"></script>
	<script src="<%= contextPath %>/bootstrap/js/bootstrap-collapse.js"></script>
	<script src="<%= contextPath %>/bootstrap/js/bootstrap-carousel.js"></script>
	<script src="<%= contextPath %>/bootstrap/js/bootstrap-typeahead.js"></script>

</body>
</html>
