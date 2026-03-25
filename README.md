<!-- Minnesota pinned at top, rule line, then all jurisdictions alphabetically by name (MN appears again). Values are 2-letter codes or OTHER. -->
<div th:fragment="pastBenefitsStateSelect(input, data)"
     th:classappend="${!data.isValid() && !inputData.valid(data)} ? 'form-group--error' : ''"
     th:with="inputData=${data.get(input.name)},formInputName=${T(org.codeforamerica.shiba.pages.PageUtils).getFormInputName(input.name)}"
     class="form-group">
  <div th:replace="~{fragments/form-question-prompt :: formQuestionPrompt(${input})}"></div>
  <div class="select">
    <select th:id="${input.name}"
            class="select__element"
            th:name="${formInputName}"
            th:attr="aria-labelledby=${input.name + '-label'},aria-invalid=${!data.isValid() && !inputData.valid(data)}">
      <option value="" th:text="#{past-benefit-details.state.select-placeholder}"></option>
      <option value="MN" th:text="#{past-benefit-details.state.MN}"
              th:selected="${inputData.value.contains('MN')}"></option>
      <option disabled value="" th:text="#{past-benefit-details.state.separator}"></option>
      <!-- Alphabetical by jurisdiction name (Minnesota included again below) -->
      <option value="AL" th:text="#{past-benefit-details.state.AL}" th:selected="${inputData.value.contains('AL')}"></option>
      <option value="AK" th:text="#{past-benefit-details.state.AK}" th:selected="${inputData.value.contains('AK')}"></option>
      <option value="AS" th:text="#{past-benefit-details.state.AS}" th:selected="${inputData.value.contains('AS')}"></option>
      <option value="AZ" th:text="#{past-benefit-details.state.AZ}" th:selected="${inputData.value.contains('AZ')}"></option>
      <option value="AR" th:text="#{past-benefit-details.state.AR}" th:selected="${inputData.value.contains('AR')}"></option>
      <option value="CA" th:text="#{past-benefit-details.state.CA}" th:selected="${inputData.value.contains('CA')}"></option>
      <option value="CO" th:text="#{past-benefit-details.state.CO}" th:selected="${inputData.value.contains('CO')}"></option>
      <option value="CT" th:text="#{past-benefit-details.state.CT}" th:selected="${inputData.value.contains('CT')}"></option>
      <option value="DE" th:text="#{past-benefit-details.state.DE}" th:selected="${inputData.value.contains('DE')}"></option>
      <option value="DC" th:text="#{past-benefit-details.state.DC}" th:selected="${inputData.value.contains('DC')}"></option>
      <option value="FL" th:text="#{past-benefit-details.state.FL}" th:selected="${inputData.value.contains('FL')}"></option>
      <option value="GA" th:text="#{past-benefit-details.state.GA}" th:selected="${inputData.value.contains('GA')}"></option>
      <option value="GU" th:text="#{past-benefit-details.state.GU}" th:selected="${inputData.value.contains('GU')}"></option>
      <option value="HI" th:text="#{past-benefit-details.state.HI}" th:selected="${inputData.value.contains('HI')}"></option>
      <option value="ID" th:text="#{past-benefit-details.state.ID}" th:selected="${inputData.value.contains('ID')}"></option>
      <option value="IL" th:text="#{past-benefit-details.state.IL}" th:selected="${inputData.value.contains('IL')}"></option>
      <option value="IN" th:text="#{past-benefit-details.state.IN}" th:selected="${inputData.value.contains('IN')}"></option>
      <option value="IA" th:text="#{past-benefit-details.state.IA}" th:selected="${inputData.value.contains('IA')}"></option>
      <option value="KS" th:text="#{past-benefit-details.state.KS}" th:selected="${inputData.value.contains('KS')}"></option>
      <option value="KY" th:text="#{past-benefit-details.state.KY}" th:selected="${inputData.value.contains('KY')}"></option>
      <option value="LA" th:text="#{past-benefit-details.state.LA}" th:selected="${inputData.value.contains('LA')}"></option>
      <option value="ME" th:text="#{past-benefit-details.state.ME}" th:selected="${inputData.value.contains('ME')}"></option>
      <option value="MD" th:text="#{past-benefit-details.state.MD}" th:selected="${inputData.value.contains('MD')}"></option>
      <option value="MA" th:text="#{past-benefit-details.state.MA}" th:selected="${inputData.value.contains('MA')}"></option>
      <option value="MI" th:text="#{past-benefit-details.state.MI}" th:selected="${inputData.value.contains('MI')}"></option>
      <option value="MN" th:text="#{past-benefit-details.state.MN}"></option>
      <option value="MS" th:text="#{past-benefit-details.state.MS}" th:selected="${inputData.value.contains('MS')}"></option>
      <option value="MO" th:text="#{past-benefit-details.state.MO}" th:selected="${inputData.value.contains('MO')}"></option>
      <option value="MT" th:text="#{past-benefit-details.state.MT}" th:selected="${inputData.value.contains('MT')}"></option>
      <option value="NE" th:text="#{past-benefit-details.state.NE}" th:selected="${inputData.value.contains('NE')}"></option>
      <option value="NV" th:text="#{past-benefit-details.state.NV}" th:selected="${inputData.value.contains('NV')}"></option>
      <option value="NH" th:text="#{past-benefit-details.state.NH}" th:selected="${inputData.value.contains('NH')}"></option>
      <option value="NJ" th:text="#{past-benefit-details.state.NJ}" th:selected="${inputData.value.contains('NJ')}"></option>
      <option value="NM" th:text="#{past-benefit-details.state.NM}" th:selected="${inputData.value.contains('NM')}"></option>
      <option value="NY" th:text="#{past-benefit-details.state.NY}" th:selected="${inputData.value.contains('NY')}"></option>
      <option value="NC" th:text="#{past-benefit-details.state.NC}" th:selected="${inputData.value.contains('NC')}"></option>
      <option value="ND" th:text="#{past-benefit-details.state.ND}" th:selected="${inputData.value.contains('ND')}"></option>
      <option value="MP" th:text="#{past-benefit-details.state.MP}" th:selected="${inputData.value.contains('MP')}"></option>
      <option value="OH" th:text="#{past-benefit-details.state.OH}" th:selected="${inputData.value.contains('OH')}"></option>
      <option value="OK" th:text="#{past-benefit-details.state.OK}" th:selected="${inputData.value.contains('OK')}"></option>
      <option value="OR" th:text="#{past-benefit-details.state.OR}" th:selected="${inputData.value.contains('OR')}"></option>
      <option value="PA" th:text="#{past-benefit-details.state.PA}" th:selected="${inputData.value.contains('PA')}"></option>
      <option value="PR" th:text="#{past-benefit-details.state.PR}" th:selected="${inputData.value.contains('PR')}"></option>
      <option value="RI" th:text="#{past-benefit-details.state.RI}" th:selected="${inputData.value.contains('RI')}"></option>
      <option value="SC" th:text="#{past-benefit-details.state.SC}" th:selected="${inputData.value.contains('SC')}"></option>
      <option value="SD" th:text="#{past-benefit-details.state.SD}" th:selected="${inputData.value.contains('SD')}"></option>
      <option value="TN" th:text="#{past-benefit-details.state.TN}" th:selected="${inputData.value.contains('TN')}"></option>
      <option value="TX" th:text="#{past-benefit-details.state.TX}" th:selected="${inputData.value.contains('TX')}"></option>
      <option value="UT" th:text="#{past-benefit-details.state.UT}" th:selected="${inputData.value.contains('UT')}"></option>
      <option value="VT" th:text="#{past-benefit-details.state.VT}" th:selected="${inputData.value.contains('VT')}"></option>
      <option value="VI" th:text="#{past-benefit-details.state.VI}" th:selected="${inputData.value.contains('VI')}"></option>
      <option value="VA" th:text="#{past-benefit-details.state.VA}" th:selected="${inputData.value.contains('VA')}"></option>
      <option value="WA" th:text="#{past-benefit-details.state.WA}" th:selected="${inputData.value.contains('WA')}"></option>
      <option value="WV" th:text="#{past-benefit-details.state.WV}" th:selected="${inputData.value.contains('WV')}"></option>
      <option value="WI" th:text="#{past-benefit-details.state.WI}" th:selected="${inputData.value.contains('WI')}"></option>
      <option value="WY" th:text="#{past-benefit-details.state.WY}" th:selected="${inputData.value.contains('WY')}"></option>
      <option value="OTHER" th:text="#{past-benefit-details.state.OTHER}" th:selected="${inputData.value.contains('OTHER')}"></option>
    </select>
  </div>
  <div th:replace="~{fragments/inputErrorFragment :: validationError(${data}, ${input})}"></div>
</div>
