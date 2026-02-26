<!--
  "Prefer not to say" for the applicantRaceAndEthnicity page.
  When checked: unchecks all applicantRaceAndEthnicity checkboxes, clears other text, hides follow-up.
  When any race (including "Some other") is selected: unchecks "Prefer not to say"; when "Some other" is selected, unchecks other race checkboxes.
-->
<div th:fragment="applicantPreferNotToSayFragment(input, data)"
     xmlns:th="http://www.thymeleaf.org"
     th:with="
       isChecked=${data.get(input.name) != null and data.get(input.name).value.size>0 and data.get(input.name).value[0]=='true' ? true : false}
     ">

  <div th:replace="~{fragments/inputs/checkbox-input :: checkbox-input(input=${input}, data=${data})}"></div>

  <script th:inline="javascript">
	$(document).ready(function () {
	  var preferNotToSayCheckbox = document.getElementsByName('preferNotToSay[]')[0];
	  var checked = preferNotToSayCheckbox.parentElement.classList.contains('is-selected');

	  var container = $('.question-with-follow-up');
	  var followupDiv = $('div[id="applicantRaceAndEthnicity-follow-up"]');

	  function uncheckAllApplicantRaceAndEthnicity() {
	    var checkboxInputs = document.querySelectorAll('input[name="applicantRaceAndEthnicity[]"]');
	    checkboxInputs.forEach(function (input) {
	      input.removeAttribute('checked');
	      input.checked = false;
	      if (input.closest('label')) input.closest('label').classList.remove('is-selected');
	    });
	    var otherTextInput = document.querySelector('input[name="otherRaceOrEthnicity[]"], input[name="otherRaceOrEthnicity"]');
	    if (otherTextInput) { otherTextInput.value = ''; }
	  }

	  if (checked) {
	    uncheckAllApplicantRaceAndEthnicity();
	    container.hide();
	    followupDiv.hide();
	  }

	  preferNotToSayCheckbox.addEventListener('change', function () {
	    var isChecked = this.parentElement.classList.contains('is-selected');
	    if (isChecked) {
	      uncheckAllApplicantRaceAndEthnicity();
	      container.hide();
	      followupDiv.hide();
	    } else {
	      container.show();
	    }
	  });

	  // When any applicantRaceAndEthnicity checkbox is selected, uncheck "Prefer not to say"
	  var applicantRaceInputs = document.querySelectorAll('input[name="applicantRaceAndEthnicity[]"]');
	  applicantRaceInputs.forEach(function (raceInput) {
	    raceInput.addEventListener('change', function () {
	      if (this.checked || (this.closest('label') && this.closest('label').classList.contains('is-selected'))) {
	        preferNotToSayCheckbox.checked = false;
	        if (preferNotToSayCheckbox.closest('label')) preferNotToSayCheckbox.closest('label').classList.remove('is-selected');
	      }
	      // When "Some other race or ethnicity" is selected, uncheck all other race checkboxes
	      if (this.value === 'SOME_OTHER_RACE_OR_ETHNICITY' && (this.checked || (this.closest('label') && this.closest('label').classList.contains('is-selected')))) {
	        applicantRaceInputs.forEach(function (other) {
	          if (other !== raceInput) {
	            other.removeAttribute('checked');
	            other.checked = false;
	            if (other.closest('label')) other.closest('label').classList.remove('is-selected');
	          }
	        });
	      }
	    });
	  });
	});
  </script>
</div>
