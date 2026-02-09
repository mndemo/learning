
  th:with="formInputName=${T(org.codeforamerica.shiba.pages.PageUtils).getFormInputName(input.name)},
                                  optionId=${option.isNone ? 'none__checkbox' : (input.name + '-' + option.value)}">
                    <label th:for="${optionId}"
                           th:id="${optionId + '-label'}"
