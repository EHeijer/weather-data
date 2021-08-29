
package com.edheijer.weatherclient.models.smhi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "value",
    "updated",
    "parameter",
    "station",
    "period",
    "position",
    "link"
})
@Generated("jsonschema2pojo")
public class SMHIResponse {

    @JsonProperty("value")
    private List<Value> value = null;
    @JsonProperty("updated")
    private Long updated;
    @JsonProperty("parameter")
    private Parameter parameter;
    @JsonProperty("station")
    private Station station;
    @JsonProperty("period")
    private Period period;
    @JsonProperty("position")
    private List<Position> position = null;
    @JsonProperty("link")
    private List<Link> link = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("value")
    public List<Value> getValue() {
        return value;
    }

    @JsonProperty("value")
    public void setValue(List<Value> value) {
        this.value = value;
    }

    @JsonProperty("updated")
    public Long getUpdated() {
        return updated;
    }

    @JsonProperty("updated")
    public void setUpdated(Long updated) {
        this.updated = updated;
    }

    @JsonProperty("parameter")
    public Parameter getParameter() {
        return parameter;
    }

    @JsonProperty("parameter")
    public void setParameter(Parameter parameter) {
        this.parameter = parameter;
    }

    @JsonProperty("station")
    public Station getStation() {
        return station;
    }

    @JsonProperty("station")
    public void setStation(Station station) {
        this.station = station;
    }

    @JsonProperty("period")
    public Period getPeriod() {
        return period;
    }

    @JsonProperty("period")
    public void setPeriod(Period period) {
        this.period = period;
    }

    @JsonProperty("position")
    public List<Position> getPosition() {
        return position;
    }

    @JsonProperty("position")
    public void setPosition(List<Position> position) {
        this.position = position;
    }

    @JsonProperty("link")
    public List<Link> getLink() {
        return link;
    }

    @JsonProperty("link")
    public void setLink(List<Link> link) {
        this.link = link;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
