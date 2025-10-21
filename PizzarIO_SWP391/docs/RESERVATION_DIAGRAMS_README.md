# 📋 RESERVATION SYSTEM DOCUMENTATION

## 🎯 Overview
This folder contains comprehensive PlantUML diagrams for the Reservation System in PizzarIO SWP391 project.

## 📊 Available Diagrams

### 1. **Class Diagram**
- **File**: `reservation-class-diagram.puml`
- **Description**: Complete class diagram showing all components, relationships, and dependencies in the Reservation system
- **Includes**: Controller, Service, Repository, Entity, DTO, Event, and WebSocket layers

### 2. **Sequence Diagrams**

#### **CRUD Operations**
- **File**: `reservation-create-sequence.puml`
- **Description**: Complete flow for creating a new reservation with validation and auto-scheduling

- **File**: `reservation-update-sequence.puml`
- **Description**: Flow for updating existing reservation with conflict checking and rescheduling

- **File**: `reservation-cancel-sequence.puml`
- **Description**: Flow for canceling reservation with cleanup and table release

- **File**: `reservation-view-sequence.puml`
- **Description**: Multiple flows for viewing reservations (list, by table, edit form)

#### **Automated Processes**
- **File**: `reservation-auto-cancel-sequence.puml`
- **Description**: Event-driven NO_SHOW processing after 15 minutes with table release logic

- **File**: `reservation-auto-lock-sequence.puml`
- **Description**: Scheduled task for automatically locking tables 90 minutes before reservation time

#### **Additional Operations**
- **File**: `reservation-open-table-sequence.puml`
- **Description**: Flow for opening table when guest arrives (marking reservation as ARRIVED)

## 🚀 How to Use

### **Online PlantUML**
1. Copy the content of any `.puml` file
2. Go to [PlantUML Online Server](http://www.plantuml.com/plantuml/uml/)
3. Paste the content and click "Submit"
4. Download the generated diagram or copy the link

### **VS Code Extension**
1. Install "PlantUML" extension
2. Open any `.puml` file
3. Use `Ctrl+Shift+P` → "PlantUML: Preview Current Diagram"

### **IntelliJ IDEA**
1. Install "PlantUML integration" plugin
2. Open any `.puml` file
3. Right-click → "PlantUML" → "Preview"

## 📋 Diagram Features

### **Class Diagram Features**
- ✅ Color-coded layers for easy identification
- ✅ Complete method signatures with parameters and return types
- ✅ Proper dependency and association relationships
- ✅ Event-driven architecture representation
- ✅ WebSocket integration
- ✅ Optimistic locking annotations
- ✅ Scheduled task annotations

### **Sequence Diagram Features**
- ✅ Detailed actor interactions
- ✅ Activation/deactivation of components
- ✅ Alternative flows (alt/else)
- ✅ Loop constructs for collections
- ✅ Error handling scenarios
- ✅ WebSocket broadcasting
- ✅ Event publishing and listening
- ✅ Database operations with locks

## 🔧 Technical Details

### **Key Components Covered**
- **CashierDashboardController**: HTTP request handling
- **ReservationService**: Core business logic with @Transactional methods
- **ReservationSchedulerService**: Task scheduling for NO_SHOW processing
- **ReservationRepository**: Data access with custom queries
- **ReservationMapper**: Entity-DTO conversion using MapStruct
- **WebSocket Integration**: Real-time table status updates
- **Event-Driven Architecture**: Spring Events for decoupling

### **Business Rules Implemented**
- ✅ Capacity validation (guests ≤ table capacity)
- ✅ Conflict detection (90-minute buffer between reservations)
- ✅ Duplicate prevention (same table + same time)
- ✅ Status management (CONFIRMED → ARRIVED/CANCELED/NO_SHOW)
- ✅ Auto-locking (90 minutes before reservation)
- ✅ Auto-cancellation (15 minutes after start time)
- ✅ Real-time notifications via WebSocket

## 📝 Notes

- All diagrams are based on actual implementation in the codebase
- Diagrams include error handling and edge cases
- WebSocket broadcasting is shown for real-time updates
- Event-driven architecture prevents circular dependencies
- Optimistic locking is implemented for concurrent access
- Scheduled tasks run automatically without manual intervention

## 🔄 Maintenance

When updating the Reservation system:
1. Update the corresponding `.puml` files
2. Regenerate diagrams to reflect changes
3. Update this README if new diagrams are added
4. Ensure diagrams remain synchronized with code implementation
