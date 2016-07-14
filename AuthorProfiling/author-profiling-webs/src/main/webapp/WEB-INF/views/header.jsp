<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page session="false"%>
<!DOCTYPE html>
<html lang="en">
<head>
<title>UAB Author Profiler</title>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<!-- Bootstrap -->
<link href="resources/css/bootstrap.min.css" rel="stylesheet"
	media="screen">
</head>


<body>
	<script src="http://code.jquery.com/jquery.js"></script>
	<script src="resources/js/bootstrap.min.js"></script>
	<div class="container">
		<div id="header">
		<div >
              <img class="media-object" src="resources/images/coral.jpeg" width="100px" height="100px" />



        </div>
			<div class="masthead">

				<div class="navbar ">

					<div class="navbar-inner">

						<div class="container">

							<ul class="nav">
								<li class="active"><a href="#">Home</a></li>
								<li><a href="#myModal" data-toggle="modal">About</a></li>
								<li class="dropdown"><a href="#" data-toggle="dropdown"
									class="dropdown-toggle">Contact <b class="caret"></b></a>
									<ul class="dropdown-menu">
										<li><a href="#">Suraj Maharjan</a></li>
										<li><a href="#">(suraj@uab.edu)</a></li>									
										<li><a href="#">Prasha Shrestha</a></li>
										<li><a href="#">(prasha@uab.edu)</a></li>
										<li class="divider"></li>
									</ul></li>
							</ul>
						</div>
					</div>
				</div>
				<!-- /.navbar -->
			</div>


			<!-- Modal -->
			<div id="myModal" class="modal hide fade" tabindex="-1" role="dialog"
				aria-labelledby="myModalLabel" aria-hidden="true">
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal"
						aria-hidden="true">X</button>
					<h3 id="myModalLabel">About</h3>
				</div>
				<div class="modal-body">
					<p>A lot of the text today is online, written by anonymous
						users. Finding out the background information of an author adds
						credibility to what they have written. In some cases, it is
						paramount to find the author and even some clues about the author
						might help. For example in forensics, it is important to find the
						profile of an author of threatening emails or emails containing
						spam and malware in them. Also, in the area of literary research,
						if there are documents with unknown or disputed authorship,
						finding out the demographics of a writer can help to narrow down
						the list of prospective authors. Most author profiling systems
						available now are either inaccurate or slow or both. For better
						prediction, we need to have larger text and more features. But
						this has a trade-off with processing time. Larger volume of text
						will mean slower prediction. So, there is a need for a system that
						can scale with the volume of data.</p>
				</div>
				<div class="modal-footer">
					<button class="btn" data-dismiss="modal" aria-hidden="true">Close</button>
				</div>
			</div>

		</div>