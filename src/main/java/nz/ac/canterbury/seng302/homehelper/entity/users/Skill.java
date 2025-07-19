package nz.ac.canterbury.seng302.homehelper.entity.users;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Skill {

    // ===== Structural / Exterior =====
    ELECTRICAL("Electrical"),
    PLUMBING("Plumbing"),
    CARPENTRY("Carpentry"),
    ROOFING("Roofing"),
    CONCRETE_MASONRY("Concrete / Masonry"),
    FRAMING("Framing"),
    DEMOLITION("Demolition"),
    EXCAVATION("Excavation"),
    EARTHMOVING("Earthmoving"),
    SITE_PREPARATION("Site Preparation"),
    FOUNDATION_WORK("Foundation Work"),
    FOUNDATION_CRACK_REPAIR("Foundation Crack Repair"),
    STEEL_FIXING("Steel Fixing"),
    RETROFITTING("Retrofitting"),
    RETAINING_WALLS("Retaining Walls"),
    CLADDING_FACADE_SYSTEMS("Cladding / Facade Systems"),
    SCAFFOLDING("Scaffolding"),
    GUTTER_SYSTEMS("Gutter Systems"),
    CHIMNEY_REPAIR("Chimney Repair"),
    PAVING("Paving"),
    STORMWATER_SYSTEMS("Stormwater Systems"),
    SEPTIC_SYSTEMS("Septic Systems"),

    // ===== Interior Finishes =====
    PAINTING("Painting"),
    FLOORING("Flooring"),
    TILING("Tiling"),
    DRYWALL_PLASTERING("Drywall / Plastering"),
    INSULATION("Insulation"),
    ACOUSTIC_INSULATION("Acoustic Insulation"),
    WATERPROOFING("Waterproofing"),
    CABINET_MAKING("Cabinet Making"),
    JOINERY("Joinery"),
    DECK_BUILDING("Deck Building"),
    FENCE_INSTALLATION("Fence Installation"),
    SPLASHBACK_INSTALLATION("Splashback Installation"),
    ANTIQUE_RESTORATION("Antique Restoration"),
    STONE_CUTTING("Stone Cutting"),
    CNC_MACHINING("CNC Machining"),

    // ===== Tech / Smart Home =====
    HVAC("HVAC (Heating, Ventilation, AC)"),
    SOLAR_INSTALLATION("Solar Installation"),
    SMART_HOME_SYSTEMS("Smart Home Systems"),
    SECURITY_SYSTEMS("Security Systems"),
    CCTV_AND_MONITORING("CCTV and Monitoring"),
    AUTOMATION_SYSTEMS("Automation Systems"),
    TELECOMMUNICATIONS_CABLING("Telecommunications Cabling"),
    DATA_NETWORKS("Data Networks"),
    LIFT_INSTALLATION("Lift Installation"),
    GAS_FITTING("Gas Fitting"),

    // ===== Design / Planning =====
    INTERIOR_DESIGN("Interior Design"),
    ARCHITECTURE("Architecture"),
    DRAFTING_CAD("Drafting / CAD"),
    SURVEYING("Surveying"),
    PROJECT_MANAGEMENT("Project Management"),
    CONTRACT_ADMINISTRATION("Contract Administration"),
    ESTIMATING_QUANTITY_SURVEYING("Estimating / Quantity Surveying"),
    BUILDING_INSPECTION("Building Inspection"),
    HEALTH_SAFETY("Health & Safety"),
    TRAFFIC_MANAGEMENT("Traffic Management"),
    RESOURCE_CONSENT_COMPLIANCE("Resource Consent / Compliance"),
    PASSIVE_HOUSE_GREEN_BUILDING("Passive House / Green Building"),
    BUILDING_CODE_CONSULTATION("Building Code Consultation"),

    // ===== Specialty Trades =====
    WELDING("Welding"),
    GLASS_GLAZING("Glass and Glazing"),
    FIRE_PROTECTION("Fire Protection"),
    ASBESTOS_REMOVAL("Asbestos Removal"),
    ELECTRICAL_ENGINEERING("Electrical Engineering"),
    STRUCTURAL_ENGINEERING("Structural Engineering"),
    CIVIL_ENGINEERING("Civil Engineering"),
    GEOTECHNICAL_ENGINEERING("Geotechnical Engineering"),
    MECHANICAL_ENGINEERING("Mechanical Engineering");

    private final String displayName;

    Skill(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Skill getEnum(String displayName) {
        return Stream.of(Skill.values()).filter(skill -> Objects.equals(skill.getDisplayName(), displayName)).findFirst().orElse(null);
    }

    public static List<String> listOfSkills() {
        return Stream.of(Skill.values()).map(Skill::getDisplayName).sorted().collect(Collectors.toList());
    }
}