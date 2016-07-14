package edu.uab.console.model;

/**
 * Created  on 3/2/14.
 */
public class Author {
    private String AuthorID;
    private Language language;
    private Documents docs;
    private Gender gender;
    private AgeGroup ageGroup;
    private Type type;

    public Author() {
        docs= new Documents();
    }

    public String getAuthorID() {
        return AuthorID;
    }

    public void setAuthorID(String authorID) {
        AuthorID = authorID;
    }


    public Documents getDocs() {
        return docs;
    }

    public void setDocs(Documents docs) {
        this.docs = docs;
    }

    public Language getLanguage() {
        return language;

    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public void setLanguage(String language) {
        this.language = Language.fromString(language.toLowerCase());
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public void setGender(String gender) {
        if (!gender.isEmpty() && !(gender == null))
            this.gender = Gender.fromString(gender.toLowerCase());
    }


    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setType(String type) {
        this.type = Type.fromString(type.toLowerCase());
    }

    public AgeGroup getAgeGroup() {
        return ageGroup;
    }

    public void setAgeGroup(AgeGroup ageGroup) {
        this.ageGroup = ageGroup;
    }

    public void setAgeGroup(String ageGroup) {
//        if (ageGroup.equalsIgnoreCase("65-XX") || ageGroup.equalsIgnoreCase("65-xx") || ageGroup.equalsIgnoreCase("65-plus") || ageGroup.equalsIgnoreCase("65-PLUS"))
//            ageGroup = "65+";
        if (!ageGroup.isEmpty() && !(ageGroup == null))
            this.ageGroup = AgeGroup.fromString(ageGroup.toLowerCase());
    }

    public String getOutputString() {
        return "<author id=\"" + this.getAuthorID() + "\"\n\t" +
                "type=\"" + this.getType() + "\"\n\t" +
                "lang=\"" + this.getLanguage() + "\"\n\t" +
                "age_group=\"" + this.getAgeGroup() + "\"\n\t" +
                "gender=\"" + this.getGender() + "\"\n" +
                "/>";
    }

    @Override
    public String toString() {
        return "Author{" +
                "AuthorID='" + AuthorID + '\'' +
                ", language=" + language +
                ", content='" + this.docs.getUrl() + '\'' +
                ", gender=" + gender +
                ", ageGroup='" + ageGroup + '\'' +
                ", type=" + type +
                '}';
    }
}

