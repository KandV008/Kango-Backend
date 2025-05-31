package dev.kandv.kango.units.models;

import dev.kandv.kango.models.Automation;
import dev.kandv.kango.models.Dashboard;
import dev.kandv.kango.models.State;
import dev.kandv.kango.models.Tag;
import dev.kandv.kango.models.enums.Color;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class StateTest {
    private State state;
    private List<Dashboard> dashboardList;
    private Dashboard dashboard1;
    private Dashboard dashboard2;

    @BeforeEach
    public void beforeEach() {
        this.state = new State();
        this.dashboardList = new ArrayList<>();
        this.dashboard1 = new Dashboard("Example Dashboard 1");
        this.dashboard2 = new Dashboard("Example Dashboard 2");

        this.dashboardList.add(dashboard1);
        this.dashboardList.add(dashboard2);
        this.state.setDashboardList(this.dashboardList);
    }

    @Test
    public void testGetAndSetFontSize(){
        State.FontSize expected = State.FontSize.LARGE;
        this.state.setFontSize(expected);
        State.FontSize result = this.state.getFontSize();

        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testGetAndSetLanguage(){
        State.Language expected = State.Language.SPANISH;
        this.state.setLanguage(expected);
        State.Language result = this.state.getLanguage();

        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testGetAndSetColorBlind(){
        State.ColorBlind expected = State.ColorBlind.ANY;
        this.state.setColorBlind(expected);
        State.ColorBlind result = this.state.getColorBlind();

        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testGetDashboardList(){
        List<Dashboard> result = this.state.getDashboardList();

        assertThat(result.size()).isEqualTo(this.dashboardList.size());
        assertThat(result).isEqualTo(this.dashboardList);
    }

    @Test
    public void testSetDashboardList(){
        List<Dashboard> dashboardList = new ArrayList<>();
        this.state.setDashboardList(dashboardList);

        List<Dashboard> result = this.state.getDashboardList();
        assertThat(result.size()).isEqualTo(0);
        assertThat(result).isEqualTo(dashboardList);
    }

    @Test
    public void testAddDashboard(){
        Dashboard dashboard = new Dashboard("Example Dashboard 1");
        List<Dashboard> dashboardList = new ArrayList<>();
        this.state.setDashboardList(dashboardList);
        this.state.addDashboard(dashboard);

        List<Dashboard> result = this.state.getDashboardList();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.getFirst()).isEqualTo(dashboard);
    }

    @Test
    public void testRemoveDashboard(){
        this.state.removeDashboard(this.dashboard1);
        List<Dashboard> dashboardList = this.state.getDashboardList();
        assertThat(dashboardList.size()).isEqualTo(1);
        assertThat(dashboardList.getFirst()).isEqualTo(this.dashboard2);
    }

    @Test
    public void testAddTagToTagList() {
        Tag newTag = new Tag("example", Color.PURPLE);

        this.state.addTagToTagList(newTag);
        List<Tag> tags = this.state.getTagList();
        Assertions.assertThat(tags).hasSize(1);
    }

    @Test
    public void testRemoveTagFromTagList() {
        Tag newTag = new Tag("example", Color.PURPLE);

        this.state.addTagToTagList(newTag);
        List<Tag> tags = this.state.getTagList();
        Assertions.assertThat(tags).hasSize(1);

        this.state.removeTagFromTagList(newTag);
        tags = this.state.getTagList();
        Assertions.assertThat(tags).isEmpty();
    }

    @Test
    public void testAddAutomationToAutomationList() {
        Automation newAutomation = new Automation();

        this.state.addAutomationToAutomationList(newAutomation);
        List<Automation> automations = this.state.getAutomationList();
        Assertions.assertThat(automations).hasSize(1);
    }

    @Test
    public void testRemoveAutomationToAutomationList() {
        Automation newAutomation = new Automation();

        this.state.addAutomationToAutomationList(newAutomation);
        List<Automation> automations = this.state.getAutomationList();
        Assertions.assertThat(automations).hasSize(1);

        this.state.removeAutomationFromAutomation(newAutomation);
        automations = this.state.getAutomationList();
        Assertions.assertThat(automations).isEmpty();
    }
}
