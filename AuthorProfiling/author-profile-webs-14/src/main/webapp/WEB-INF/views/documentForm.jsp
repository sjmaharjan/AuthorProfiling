<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@include file="header.jsp"%>
<script src="resources/js/Chart.min.js"></script>


<form:form id="form" modelAttribute="document">
	<fieldset>
		<legend class="muted" > Author Profile</legend>
		<div class="progress progress-striped active">
			<div class="bar" style="width: 100%;"></div>
		</div>
		<div class="alert alert-success" id="result">
			<button type="button" class="close" data-dismiss="alert">&times;</button>
			<p></p>

		</div>
		<div class="row">
		<div class="span5">

<label> Language:</label>
<label class="radio inline">
<form:radiobutton path="language" value="en"  checked="checked" />English

</label>
<label class="radio inline">
 <form:radiobutton path="language" value="es"   />Spanish
</label>
		

<span class="help-block"></span>
<label>Content:</label>
		<form:textarea path="content" placeholder="Paste anonymous text" rows="10"
			class="span5" />
		<span class="help-block">Predict gender and age group of anonymous authors from their text</span>
		<button type="submit" class="btn" id="submit">Predict</button>
		</div>
		<div class="span5  pull-right">
        <canvas id="canvas" width= "390" height= "300"></canvas>
        </div>
        </div>
	</fieldset>
</form:form>



<script type="text/javascript">



	$(document).ready(function() {
        var options = {scaleOverride : true,
        scaleSteps : 10,
        scaleStartValue : 0,
        scaleFontSize : 10,
        barValueSpacing : 10}
        $('.progress').hide();
        var form = $('#form'); // contact form
        var submit = $('#submit'); // submit button
        var result = $('#result'); // alert div for show alert message
        result.hide();
        // form submit event
        form.on('submit', function(e) {
            e.preventDefault(); // prevent default form submit

            if($("#content").val().trim() == "") {
                result.hide();
                $("#canvas").hide();

            }else{
            $.ajax({
                url : '', // form action url
                type : 'POST', // form submit method get/post
                dataType : 'json', // request type html/json/xml
                data : form.serialize(), // serialize form data
                beforeSend : function() {
                    $('.progress').show();
                    submit.html('Predicting...'); // change submit button text
                },
                success : function(data) {
                     $('.progress').fadeOut(50);
                    result.html("Prediction: " + data.prediction.replace("_", ", ")).fadeIn(100); // fade in response data
                    options["scaleStepWidth"] = Math.ceil(Math.max.apply(Math, data.probs) * 10)/100;
                    submit.html('Predict'); // reset submit button text


            var barChartData = {
                labels : ["18-24_male","18-24_female","25-34_male","25-34_female","35-49_male","35-49_female", "50-64_male", "50-64_female", "65-plus_male", "65-plus_female"],
                datasets : [
                    {
                        fillColor : "rgba(151,187,205,0.5)",
                        strokeColor : "rgba(151,187,205,1)",
                        data : data.probs
                    }
                ]

            }
                $("#canvas").show();

    var myLine = new Chart(document.getElementById("canvas").getContext("2d")).Bar(barChartData, options);

                },
                error : function(e) {
                    console.log(e)
                }
            });
                }
        });

    });

</script>

<%@include file="footer.jsp"%>
