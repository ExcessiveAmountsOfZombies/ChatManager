package com.epherical.chatmanager.compat.placeholders;

import com.epherical.chatmanager.placeholders.PlaceHolderManager;
import com.epherical.chatmanager.util.PlaceHolderContext;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.node.types.PermissionNode;
import net.luckperms.api.query.QueryMode;
import net.minecraft.resources.ResourceLocation;

import java.time.Instant;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class LuckPermsPlaceholders {

    private final LuckPerms luckPerms;

    public LuckPermsPlaceholders(LuckPerms api) {
        this.luckPerms = api;
        registerDefaults();
    }

    private void registerDefaults() {
        // already present
        PlaceHolderManager.registerString(
                ResourceLocation.fromNamespaceAndPath("luckperms", "prefix"),
                this::getPrefix
        );
        PlaceHolderManager.registerString(
                ResourceLocation.fromNamespaceAndPath("luckperms", "suffix"),
                this::getSuffix
        );

        /*PlaceHolderManager.registerString(
                ResourceLocation.fromNamespaceAndPath("luckperms", "meta"),
                this::getMeta
        );
        PlaceHolderManager.registerString(
                ResourceLocation.fromNamespaceAndPath("luckperms", "meta_all"),
                this::getMetaAll
        );
        PlaceHolderManager.registerString(
                ResourceLocation.fromNamespaceAndPath("luckperms", "prefix_element"),
                this::getPrefixElement
        );
        PlaceHolderManager.registerString(
                ResourceLocation.fromNamespaceAndPath("luckperms", "suffix_element"),
                this::getSuffixElement
        );
        PlaceHolderManager.registerString(
                ResourceLocation.fromNamespaceAndPath("luckperms", "context"),
                this::getContext
        );
        PlaceHolderManager.registerString(
                ResourceLocation.fromNamespaceAndPath("luckperms", "groups"),
                this::getGroups
        );
        PlaceHolderManager.registerString(
                ResourceLocation.fromNamespaceAndPath("luckperms", "inherited_groups"),
                this::getInheritedGroups
        );
        PlaceHolderManager.registerString(
                ResourceLocation.fromNamespaceAndPath("luckperms", "primary_group_name"),
                this::getPrimaryGroupName
        );
        PlaceHolderManager.registerString(
                ResourceLocation.fromNamespaceAndPath("luckperms", "has_permission"),
                this::hasPermission
        );
        PlaceHolderManager.registerString(
                ResourceLocation.fromNamespaceAndPath("luckperms", "inherits_permission"),
                this::inheritsPermission
        );
        PlaceHolderManager.registerString(
                ResourceLocation.fromNamespaceAndPath("luckperms", "check_permission"),
                this::checkPermission
        );
        PlaceHolderManager.registerString(
                ResourceLocation.fromNamespaceAndPath("luckperms", "in_group"),
                this::inGroup
        );
        PlaceHolderManager.registerString(
                ResourceLocation.fromNamespaceAndPath("luckperms", "inherits_group"),
                this::inheritsGroup
        );
        PlaceHolderManager.registerString(
                ResourceLocation.fromNamespaceAndPath("luckperms", "has_groups_on_track"),
                this::hasGroupsOnTrack
        );
        PlaceHolderManager.registerString(
                ResourceLocation.fromNamespaceAndPath("luckperms", "highest_group_by_weight"),
                this::getHighestGroupByWeight
        );
        PlaceHolderManager.registerString(
                ResourceLocation.fromNamespaceAndPath("luckperms", "lowest_group_by_weight"),
                this::getLowestGroupByWeight
        );
        PlaceHolderManager.registerString(
                ResourceLocation.fromNamespaceAndPath("luckperms", "highest_inherited_group_by_weight"),
                this::getHighestInheritedGroupByWeight
        );
        PlaceHolderManager.registerString(
                ResourceLocation.fromNamespaceAndPath("luckperms", "lowest_inherited_group_by_weight"),
                this::getLowestInheritedGroupByWeight
        );
        PlaceHolderManager.registerString(
                ResourceLocation.fromNamespaceAndPath("luckperms", "current_group_on_track"),
                this::getCurrentGroupOnTrack
        );
        PlaceHolderManager.registerString(
                ResourceLocation.fromNamespaceAndPath("luckperms", "next_group_on_track"),
                this::getNextGroupOnTrack
        );
        PlaceHolderManager.registerString(
                ResourceLocation.fromNamespaceAndPath("luckperms", "previous_group_on_track"),
                this::getPreviousGroupOnTrack
        );
        PlaceHolderManager.registerString(
                ResourceLocation.fromNamespaceAndPath("luckperms", "first_group_on_tracks"),
                this::getFirstGroupOnTracks
        );
        PlaceHolderManager.registerString(
                ResourceLocation.fromNamespaceAndPath("luckperms", "last_group_on_tracks"),
                this::getLastGroupOnTracks
        );
        PlaceHolderManager.registerString(
                ResourceLocation.fromNamespaceAndPath("luckperms", "expiry_time"),
                this::getExpiryTime
        );
        PlaceHolderManager.registerString(
                ResourceLocation.fromNamespaceAndPath("luckperms", "inherited_expiry_time"),
                this::getInheritedExpiryTime
        );
        PlaceHolderManager.registerString(
                ResourceLocation.fromNamespaceAndPath("luckperms", "group_expiry_time"),
                this::getGroupExpiryTime
        );
        PlaceHolderManager.registerString(
                ResourceLocation.fromNamespaceAndPath("luckperms", "inherited_group_expiry_time"),
                this::getInheritedGroupExpiryTime
        );*/
    }

    private String getPrefix(PlaceHolderContext player, String... params) {
        if (player == null) return "";
        User user = luckPerms.getUserManager().getUser(player.getPlayer().getUUID());
        if (user == null) return "";
        CachedMetaData meta = user.getCachedData().getMetaData();
        return meta.getPrefix() == null ? "" : meta.getPrefix();
    }

    private String getSuffix(PlaceHolderContext player, String... params) {
        if (player == null) return "";
        User user = luckPerms.getUserManager().getUser(player.getPlayer().getUUID());
        if (user == null) return "";
        CachedMetaData meta = user.getCachedData().getMetaData();
        return meta.getSuffix() == null ? "" : meta.getSuffix();
    }


    /** Attempts to resolve a LuckPerms {@link User} from the placeholder context. */
    private User user(PlaceHolderContext ctx) {
        if (ctx == null) return null;
        return luckPerms.getUserManager().getUser(ctx.getPlayer().getUUID());
    }


    /*private String getMeta(PlaceHolderContext ctx, String... params) {
        User u = user(ctx);
        if (u == null || params.length == 0) return "";
        String value = u.getCachedData().getMetaData().getMetaValue(params[0]);
        return value == null ? "" : value;
    }

    private String getMetaAll(PlaceHolderContext ctx, String... params) {
        User u = user(ctx);
        if (u == null || params.length == 0) return "";
        var meta = u.getCachedData().getMetaData().getMeta();
        var values = meta.get(params[0]);               // multimap – might be empty
        return values == null ? "" : String.join(", ", values);
    }


    private String getPrefixElement(PlaceHolderContext ctx, String... params) {
        return getPrefix(ctx, params);   // simple fallback – refine if needed
    }

    private String getSuffixElement(PlaceHolderContext ctx, String... params) {
        return getSuffix(ctx, params);
    }


    private String getContext(PlaceHolderContext ctx, String... params) {
        User u = user(ctx);
        if (u == null) return "";
        var contextMgr = luckPerms.getContextManager();
        var set = contextMgr.getContext(u).orElseGet(() -> contextMgr.getStaticContext());

        if (params.length > 0) {
            return String.join(", ", set.getValues(params[0]));
        }
        // pretty print whole set: key1=value1,key2=value2
        return set.entrySet().stream()
                  .map(e -> e.getKey() + '=' + e.getValue())
                  .collect(Collectors.joining(", "));
    }


    private String getGroups(PlaceHolderContext ctx, String... params) {
        User u = user(ctx);
        if (u == null) return "";
        return u.getNodes(NodeType.INHERITANCE).stream()
                .filter(node -> node instanceof InheritanceNode in
                                && !in.isTemporary()
                                && !in.isNegated())              // only positive, direct
                .map(in -> ((InheritanceNode) in).getGroupName())
                .distinct()
                .collect(Collectors.joining(", "));
    }

    private String getInheritedGroups(PlaceHolderContext ctx, String... params) {
        User u = user(ctx);
        if (u == null) return "";
        var qOpts = luckPerms.getContextManager()
                             .getQueryOptions(u)
                             .orElseGet(luckPerms.getContextManager()::getStaticQueryOptions);

        return luckPerms.getInheritanceHandler().getGroups(u, qOpts).stream()
                .map(g -> g.getName())
                .collect(Collectors.joining(", "));
    }

    private String getPrimaryGroupName(PlaceHolderContext ctx, String... p) {
        User u = user(ctx);
        return u == null ? "" : u.getPrimaryGroup();
    }

    private String hasPermission(PlaceHolderContext ctx, String... params) {
        User u = user(ctx);
        if (u == null || params.length == 0) return "false";
        String perm = params[0];
        boolean hasDirect = u.getNodes(NodeType.PERMISSION).stream()
                .filter(n -> n instanceof PermissionNode pn
                             && pn.getPermission().equalsIgnoreCase(perm))
                .anyMatch(net.luckperms.api.node.Node::getValue);
        return Boolean.toString(hasDirect);
    }

    private String inheritsPermission(PlaceHolderContext ctx, String... params) {
        User u = user(ctx);
        if (u == null || params.length == 0) return "false";
        var tristate = u.getCachedData()
                        .getPermissionData()
                        .checkPermission(params[0])
                        .result();
        return Boolean.toString(tristate.asBoolean());
    }

    private String checkPermission(PlaceHolderContext ctx, String... params) {
        return inheritsPermission(ctx, params);   // same as above, but name kept for clarity
    }


    private String inGroup(PlaceHolderContext ctx, String... params) {
        User u = user(ctx);
        if (u == null || params.length == 0) return "false";
        String group = params[0];
        boolean direct = u.getNodes(NodeType.INHERITANCE).stream()
                .filter(n -> n instanceof InheritanceNode in
                             && in.getGroupName().equalsIgnoreCase(group))
                .anyMatch(net.luckperms.api.node.Node::getValue);
        return Boolean.toString(direct);
    }

    private String inheritsGroup(PlaceHolderContext ctx, String... params) {
        User u = user(ctx);
        if (u == null || params.length == 0) return "false";
        var qOpts = luckPerms.getContextManager()
                             .getQueryOptions(u)
                             .orElseGet(luckPerms.getContextManager()::getStaticQueryOptions);
        boolean inherits = luckPerms.getInheritanceHandler()
                                    .getGroups(u, qOpts).stream()
                                    .anyMatch(g -> g.getName().equalsIgnoreCase(params[0]));
        return Boolean.toString(inherits);
    }



    private String hasGroupsOnTrack(PlaceHolderContext ctx, String... params) {
        User u = user(ctx);
        if (u == null || params.length == 0) return "false";
        var track = luckPerms.getTrackManager().getTrack(params[0]);
        if (track == null) return "false";
        var groups = track.getGroups();
        return Boolean.toString(groups.stream().anyMatch(g -> inheritsGroup(ctx, g).equals("true")));
    }

    // current / next / previous group on track
    private String getCurrentGroupOnTrack(PlaceHolderContext ctx, String... params) {
        User u = user(ctx);
        if (u == null || params.length == 0) return "";
        var track = luckPerms.getTrackManager().getTrack(params[0]);
        if (track == null) return "";
        return track.getCurrentGroup(u.getPrimaryGroup());
    }

    private String getNextGroupOnTrack(PlaceHolderContext ctx, String... params) {
        User u = user(ctx);
        if (u == null || params.length == 0) return "";
        var track = luckPerms.getTrackManager().getTrack(params[0]);
        if (track == null) return "";
        return track.getNext(u.getPrimaryGroup()).orElse("");
    }

    private String getPreviousGroupOnTrack(PlaceHolderContext ctx, String... params) {
        User u = user(ctx);
        if (u == null || params.length == 0) return "";
        var track = luckPerms.getTrackManager().getTrack(params[0]);
        if (track == null) return "";
        return track.getPrevious(u.getPrimaryGroup()).orElse("");
    }

    *//* First / last group on multiple tracks *//*
    private String getFirstGroupOnTracks(PlaceHolderContext ctx, String... params) {
        User u = user(ctx);
        if (u == null || params.length == 0) return "";
        for (String t : params[0].split(",")) {
            var track = luckPerms.getTrackManager().getTrack(t.trim());
            if (track == null) continue;
            var current = track.getCurrentGroup(u.getPrimaryGroup());
            if (current != null) return current;
        }
        return "";
    }

    private String getLastGroupOnTracks(PlaceHolderContext ctx, String... params) {
        User u = user(ctx);
        if (u == null || params.length == 0) return "";
        String last = "";
        for (String t : params[0].split(",")) {
            var track = luckPerms.getTrackManager().getTrack(t.trim());
            if (track == null) continue;
            var current = track.getCurrentGroup(u.getPrimaryGroup());
            if (current != null) last = current;
        }
        return last;
    }


    private String getHighestGroupByWeight(PlaceHolderContext ctx, String... p) {
        return groupByWeight(ctx, *//*highest=*//*true, *//*includeInherited=*//*false);
    }

    private String getLowestGroupByWeight(PlaceHolderContext ctx, String... p) {
        return groupByWeight(ctx, *//*highest=*//*false, *//*includeInherited=*//*false);
    }

    private String getHighestInheritedGroupByWeight(PlaceHolderContext ctx, String... p) {
        return groupByWeight(ctx, *//*highest=*//*true, *//*includeInherited=*//*true);
    }

    private String getLowestInheritedGroupByWeight(PlaceHolderContext ctx, String... p) {
        return groupByWeight(ctx, *//*highest=*//*false, *//*includeInherited=*//*true);
    }

    private String groupByWeight(PlaceHolderContext ctx,
                                 boolean highest,
                                 boolean includeInherited) {

        User u = user(ctx);
        if (u == null) return "";

        Stream<net.luckperms.api.model.group.Group> stream;
        var qm = luckPerms.getContextManager();
        var qOpts = qm.getQueryOptions(u).orElseGet(qm::getStaticQueryOptions);

        if (includeInherited) {
            stream = luckPerms.getInheritanceHandler()
                              .getGroups(u, qOpts).stream();
        } else {
            stream = u.getNodes(NodeType.INHERITANCE).stream()
                    .filter(node -> node instanceof InheritanceNode in && in.getValue())
                    .map(in -> luckPerms.getGroupManager().getGroup(((InheritanceNode) in).getGroupName()))
                    .filter(Objects::nonNull);
        }

        Comparator<net.luckperms.api.model.group.Group> cmp =
                Comparator.comparingInt(g -> g.getWeight().orElse(0));

        return stream.min(highest ? cmp.reversed() : cmp)
                     .map(net.luckperms.api.model.group.Group::getName)
                     .orElse("");
    }

    private String getExpiryTime(PlaceHolderContext ctx, String... params) {
        return expiry(ctx, params, *//*includeInherited=*//*false, *//*group=*//*false);
    }

    private String getInheritedExpiryTime(PlaceHolderContext ctx, String... params) {
        return expiry(ctx, params, *//*includeInherited=*//*true, *//*group=*//*false);
    }

    private String getGroupExpiryTime(PlaceHolderContext ctx, String... params) {
        return expiry(ctx, params, *//*includeInherited=*//*false, *//*group=*//*true);
    }

    private String getInheritedGroupExpiryTime(PlaceHolderContext ctx, String... params) {
        return expiry(ctx, params, *//*includeInherited=*//*true, *//*group=*//*true);
    }

    private String expiry(PlaceHolderContext ctx,
                          String[] params,
                          boolean includeInherited,
                          boolean groupNode) {

        User u = user(ctx);
        if (u == null || params.length == 0) return "";

        String key = params[0];

        Stream<? extends net.luckperms.api.node.Node> nodes = (includeInherited
                ? u.resolveInheritedNodes(QueryMode.NON_CONTEXTUAL)
                : u.getNodes());

        return nodes.filter(n -> {
                    if (groupNode && n instanceof InheritanceNode in) {
                        return in.getGroupName().equalsIgnoreCase(key);
                    }
                    if (!groupNode && n instanceof PermissionNode pn) {
                        return pn.getPermission().equalsIgnoreCase(key);
                    }
                    return false;
                })
                .filter(net.luckperms.api.node.Node::hasExpiry)
                .findFirst()
                .map(n -> String.valueOf(n.getExpiry().getEpochSecond() - Instant.now().getEpochSecond()))
                .orElse("");
    }*/
}
