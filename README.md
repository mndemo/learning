  - &veteransBenefitsIncomeSource
    name: veteransBenefitsIncomeSource
    pageTitle: unearned-income-source.title
    headerKey: veterans-benefits-income-source.who-receives
    headerHelpMessageKey: unearned-income-source.you-can-select-more
    contextFragment: personIncomeContextFragment
    inputs:
      - type: CUSTOM
        name: monthlyIncomeVeteransBenefits
        helpMessageKey: general.if-you-dont-have
        customInputFragment: householdOptionsCheckboxesWithFollowup
        customFollowUps: 'true'
        validators:
          - validation: NOT_BLANK
            errorMessageKey: general.validation.make-sure-to-select-a-person
        options:
          datasources:
            - pageName: personalInfo
            - groupName: household
        followUps:
          # Amount
          - name: veteransBenefitsAmount
            type: MONEY
            inputPostfix: general.input.postfix.per-month
            validators:
              - validation: MONEY
                errorMessageKey: general.validation.make-sure-you-enter-a-dollar-amount
                condition:
                  input: veteransBenefitsAmount
                  matcher: NOT_EMPTY
            promptMessage:
              promptMessageKey: unearned-income-source.how-much-income-monthly

          # Frequency
          - name: veteransBenefitsFrequency
            type: SELECT
            validators:
              - validation: NOT_BLANK
                errorMessageKey: general.validation.make-sure-you-answer-this-question
            options:
              selectableOptions:
                - value: MONTHLY
                  messageKey: unearned-income.frequency.monthly
                - value: TWICE_A_MONTH
                  messageKey: unearned-income.frequency.twice-a-month
                - value: WEEKLY
                  messageKey: unearned-income.frequency.weekly
                - value: OTHER
                  messageKey: unearned-income.frequency.other
            promptMessage:
              promptMessageKey: unearned-income-source.frequency-of-income

          # Start date
          - name: veteransBenefitsStartDate
            type: DATE
            promptMessage:
              promptMessageKey: unearned-income-source.start-date

          # End date
          - name: veteransBenefitsEndDate
            type: DATE
            promptMessage:
              promptMessageKey: unearned-income-source.end-date
            helpMessageKey: unearned-income-source.no-end-date-help
