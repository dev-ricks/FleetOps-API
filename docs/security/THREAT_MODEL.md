# Threat Model – FleetOps API

This document summarizes the key assets, threat actors, attack vectors, and mitigations relevant to the FleetOps API. It is intended to guide security planning and risk management for the system.

## Key Assets
Key assets include driver personally identifiable information (PII), vehicle operational data, and API credentials and tokens. Protecting these assets is critical to maintaining trust and compliance.

## Threat Actors
Potential threat actors include external attackers (such as cybercriminals or competitors), malicious insiders, and compromised accounts. Each actor poses different risks to the system.

## Attack Vectors
The system may be targeted through various attack vectors, including unauthorized access to API endpoints, injection attacks (such as SQL injection and cross-site scripting), credential theft, denial-of-service attacks, and supply chain compromise.

## Mitigations
Mitigation strategies include implementing OAuth2/JWT authentication and role-based access control, validating and encoding all input, encrypting data at rest and in transit, enforcing rate limiting and security headers, logging security events, and conducting regular vulnerability scanning and patching. These measures collectively reduce the risk of successful attacks and support compliance with security requirements.
