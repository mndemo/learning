  # Footer build label: Spring reads SHIBA_BUILD_VERSION at runtime (application.yaml)
        oc set env deployment/mnbenefits-prod-deployment SHIBA_BUILD_VERSION=${{ steps.fetch-latest-release.outputs.tag_name }} -n mn-benefits-prod
