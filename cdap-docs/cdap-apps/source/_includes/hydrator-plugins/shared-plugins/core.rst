.. meta::
    :author: Cask Data, Inc.
    :copyright: Copyright • 2015 Cask Data, Inc.

.. _cdap-apps-etl-plugins-shared-core-validator:

=============================
Shared Plugins: CoreValidator
=============================

.. rubric:: Description

A system-supplied validator that offers a set of functions that can be called from the validator transform.

It is included in a transform by adding its name (``core``) to the ``validators`` field of
the transform configuration and its functions are referenced by using its JavaScript name
(``coreValidator``). See an example in the Transforms Validator plugin.

.. rubric:: Use Case

Users often want to validate the input for a certain data-type or format or to check if
they are a valid date, credit card, etc. It's useful to implement and aggregate these
common functions and make them easily available.

.. rubric:: Functions

This table lists the methods available in CoreValidator that can be called from the ValidatorTransform:

.. csv-table::
   :header: "Command","Description"
   :widths: 40,60
   
   "``isDate(String date)``","Returns true if the passed param is a valid date."
   "``isCreditCard(String card)``","Returns true if the passed param is a valid CreditCard."
   "``isBlankOrNull(String val)``","Checks if the field is null and length of the field is greater than zero not including whitespace."
   "``isEmail(String email)``","Checks if a field has a valid e-mail address."
   "``isInRange(double value, double min, double max)``","Checks if a value is within a range."
   "``isInRange(int value, int min, int max)``","Checks if a value is within a range."
   "``isInRange(float value, float min, float max)``","Checks if a value is within a range."
   "``isInRange(short value, short min, short max)``","Checks if a value is within a range."
   "``isInRange(long value, long min, long max)``","Checks if a value is within a range."
   "``isInt(String input)``","Checks if the value can safely be converted to a int primitive."
   "``isLong(String input)``","Checks if the value can safely be converted to a long primitive."
   "``isShort(String input)``","Checks if the value can safely be converted to a short primitive."
   "``isUrl(String input)``","Checks if the value can safely be converted to a int primitive."
   "``matchRegex(String pattern, String input)``","Checks if the value matches the regular expression."
   "``maxLength(String input, int maxLength)``","Checks if the value's adjusted length is less than or equal to the max."
   "``maxValue(double val, double maxVal)``","Checks if the value is less than or equal to the max."
   "``maxValue(long val, long maxVal)``","Checks if the value is less than or equal to the max."
   "``maxValue(int val, int maxVal)``","Checks if the value is less than or equal to the max."
   "``maxValue(float val, float maxVal)``","Checks if the value is less than or equal to the max."
   "``minValue(double val, double minVal)``","Checks if the value is greater than or equal to the min."
   "``minValue(long val, long minVal)``","Checks if the value is greater than or equal to the min."
   "``minValue(int val, int minVal)``","Checks if the value is greater than or equal to the min."
   "``minValue(float val, float minVal)``","Checks if the value is greater than or equal to the min."
   "``minLength(String input, int length)``","Checks if the value's adjusted length is greater than or equal to the min."
   "``isValidISBN(String isbn)``","Checks if the code is either a valid ISBN-10 or ISBN-13 code."
   "``isValidInet4Address(String ipv4)``","Validates an IPv4 address."
   "``isValidInet6Address(String ipv6)``","Validates an IPv6 address."
   "``isValidIp(String ip)``","Checks if the specified string is a valid IP address."
   "``isValidCountryCodeTid(String ccTld)``","Returns true if the specified ``String`` matches any IANA-defined country code top-level domain."
   "``isValidGenericTId(String gTld)``","Returns true if the specified ``String`` matches any IANA-defined generic top-level domain."
   "``isValidInfrastructureTId(String iTld)``","Returns true if the specified ``String`` matches any IANA-defined infrastructure top-level domain."
   "``isValidLocalTId(String lTld)``","Returns true if the specified ``String`` matches any widely used ""local"" domains (localhost or localdomain)."
   "``isValidTId(String tld)``","Returns true if the specified ``String`` matches any IANA-defined top-level domain."

