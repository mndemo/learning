/* Shared site banners: demo, SNAP scam alert, and child tax credit notice.
   Fully defined here so appearance does not depend on Honeycrisp. */
.demo-banner,
.alert-banner,
.delayed-processing-time-notice-box {
	position: relative;
	z-index: 2;
	margin-left: -1.5rem;
	margin-right: -1.5rem;
	padding: 1.5rem;
	background-color: #FFBA26;
	text-align: center;
	font-size: 1.6rem;
	line-height: 1.5;
	color: #121111;
	border: none;
	box-sizing: border-box;
	width: auto;
}

@media screen and (min-width: 601px) {
	.demo-banner,
	.alert-banner,
	.delayed-processing-time-notice-box {
		margin-left: -3.5rem;
		margin-right: -3.5rem;
		padding-left: 3.5rem;
		padding-right: 3.5rem;
	}
}

/* Keep nested Honeycrisp utility classes from changing banner typography */
.demo-banner .text-25,
.demo-banner .text-bold,
.alert-banner .text-25,
.alert-banner .text-bold,
.delayed-processing-time-notice-box .text-25,
.delayed-processing-time-notice-box .text-bold {
	font-size: inherit;
	line-height: inherit;
	margin-bottom: 0;
	color: inherit;
}

.demo-banner .text-bold,
.alert-banner .text-bold,
.delayed-processing-time-notice-box .text-bold {
	font-weight: 700;
}

.link--alert-banner,
.demo-banner a,
.alert-banner a,
.delayed-processing-time-notice-box a {
	color: #003865;
}

.link--alert-banner:visited,
.demo-banner a:visited,
.alert-banner a:visited,
.delayed-processing-time-notice-box a:visited {
	color: #5B0E5D;
}
