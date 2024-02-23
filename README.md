
# Project Summary: Military Communication System

This project aims to provide fast and efficient communication for the armed forces, allowing the exchange of information such as text messages, tactical instructions, and authorizations. The system follows a hierarchical structure based on military ranks, with three distinct profiles, where the lower profile can only execute instructions approved by higher-ranking profiles.
## Key Features
### Communication Between Entities
- Sending text messages between individual entities.
- Exchange of tactical instructions, such as equipment movement.
- Authorizations for missile launches.
### Group Communication
- Sending messages to groups of entities based on military ranks or other defined criteria.
- Creation and definition of channels where all members receive transmitted messages.
### Military Hierarchy
- Hierarchical structure based on military ranks.
- Lower profiles require approval from higher-ranking profiles to execute critical instructions.

## Technical Implementation
### Communication Protocols
- Utilization of TCP and UDP protocols to ensure secure and efficient communication.

### Thread Synchronization
- Implementation of synchronized threads to handle concurrency when accessing shared resources.
- Ensuring multiple processes can access and modify data in a coordinated manner.


### Multithreaded Server

- Development of a multithreaded server capable of handling multiple connections simultaneously.
- Improvement in system scalability and performance.
