<!DOCTYPE html>
<html th:lang="${#locale.language}" xmlns:th="http://www.thymeleaf.org">
<th:block th:fragment="yesNoRadioInline (input, data)"
          th:with="inputData=${data.get(input.name)},
                   formInputName=${T(org.codeforamerica.shiba.pages.PageUtils).getFormInputName(input.name)},
                   hasError=${!data.isValid() && !inputData.valid(data)},
                   hasHelpMessage=${input.helpMessageKey != null},
                   needsAriaLabel=${input.needsAriaLabel()},
                   hasAriaDescribedbyInput=${input.ariaDescribedbyInput != null},
                   ariaDescribedbyId=${hasAriaDescribedbyInput ? input.ariaDescribedbyInput + '-help-message' : (hasHelpMessage ? input.name + '-help-message' : '')}">

    <div class="form-group"
         th:classappend="${!inputData.valid(data)} ? 'form-group--error' : ''">
        <fieldset>
            <div th:replace="~{fragments/form-question-prompt :: formQuestionPrompt(${input})}"></div>
            <p th:id="${ariaDescribedbyId}" class="text--help"
               th:if="${input.helpMessageKey != null}"
               th:text="#{${input.helpMessageKey }}"></p>

            <div class="radio-inline-group">
                <label th:for="${#ids.next(input.name)}"
                       class="radio-button"
                       style="display:inline-block;margin-right:1.5rem;">
                    <input type="radio"
                           th:name="${formInputName}"
                           th:id="${#ids.seq(input.name)}"
                           value="true"
                           th:checked="${inputData.value.contains('true')}"
                           th:attr="aria-describedby=${ariaDescribedbyId},
                                    aria-invalid=${hasError},
                                    aria-label=${needsAriaLabel ? input.name : ''}"
                           th:attrappend="data-follow-up=${input.followUpValues.contains('true')} ? |#${input.name}-follow-up| : ''">
                    <span th:text="#{general.inputs.yes}"></span>
                </label>

                <label th:for="${#ids.next(input.name)}"
                       class="radio-button"
                       style="display:inline-block;margin-right:1.5rem;">
                    <input type="radio"
                           th:name="${formInputName}"
                           th:id="${#ids.seq(input.name)}"
                           value="false"
                           th:checked="${inputData.value.contains('false')}"
                           th:attr="aria-describedby=${ariaDescribedbyId},
                                    aria-invalid=${hasError},
                                    aria-label=${needsAriaLabel ? input.name : ''}"
                           th:attrappend="data-follow-up=${input.followUpValues.contains('false')} ? |#${input.name}-follow-up| : ''">
                    <span th:text="#{general.inputs.no}"></span>
                </label>
            </div>
        </fieldset>

        <div th:replace="~{fragments/inputErrorFragment :: validationError(${data}, ${input})}"></div>
    </div>
</th:block>
</html>

