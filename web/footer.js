document.write(	 
"<footer>" +
"	<hr>" +
"	<div class='text-center'>" +
"		<p><a rel='license' href='http://creativecommons.org/licenses/by/4.0/'><img alt='Creative Commons License' style='border-width:0' src='https://i.creativecommons.org/l/by/4.0/88x31.png' /></a><br /><span xmlns:dct='http://purl.org/dc/terms/' href='http://purl.org/dc/dcmitype/Dataset' property='dct:title' rel='dct:type'>&copy JCAlgTest</span> by <span xmlns:cc='http://creativecommons.org/ns#' property='cc:attributionName'>CRoCS MU</span> is licensed under a <a rel='license' href='http://creativecommons.org/licenses/by/4.0/'>Creative Commons Attribution 4.0 International License</a>.</p>" +
"	</div>" +
"</footer>" +
"<script>/* <![CDATA[ */(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){(i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),m=s.getElementsByTagName(o)" +
"[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)})(window,document,'script','//www.google-analytics.com/analytics.js','ga');ga('create','UA-1931909-2','auto');ga('send','pageview');/* ]]> */</script>" 
);

jQuery(document).ready(function(){
	var offset = 220;var duration = 500;
		jQuery(window).scroll(function(){
	if (jQuery(this).scrollTop()>offset){jQuery('.back-to-top').fadeIn(duration);
	}else{jQuery('.back-to-top').fadeOut(duration);}});
		jQuery('.back-to-top').click(function(event){event.preventDefault();
		jQuery('html, body').animate({scrollTop: 0}, duration);
	return false;})});