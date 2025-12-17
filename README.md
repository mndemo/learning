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

   
</th:block>
</html>

