#!/bin/bash

export version=$1

echo "Uploading documentation..."

(cd ../errai-docs/target/docbook/publish/en/pdf && mv Reference_Guide.pdf Errai_${version}_Reference_Guide.pdf)

(cd ../errai-docs/target/docbook/publish/en  && scp -rp . errai@filemgmt.jboss.org:/docs_htdocs/errai/$version/errai/reference)

(cd ../errai-docs/src/main/asciidoc && scp -rp author errai@filemgmt.jboss.org:/docs_htdocs/errai/$version/errai/html && scp -rp author errai@filemgmt.jboss.org:/docs_htdocs/errai/$version/errai/reference/html_single)

echo "Done!"

